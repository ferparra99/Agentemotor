# Code Review — `expired_policies.py`

## Resumen

Se revisa un endpoint Flask que lista pólizas vencidas de un asesor. El código presentado es funcional en un entorno de prototipo pero tiene problemas graves que impedirían su paso a producción. A continuación se analizan los problemas reales ordenados por criticidad.

---

## Problema 1: Inyección SQL por concatenación de cadenas en `executescript`

**Línea**: No visible directamente, pero el uso de `cursor.execute()` con parámetros posicionales (`?`) mitiga parcialmente el riesgo. Sin embargo, el mayor riesgo está en la **ausencia de gestión de conexiones**.

**Qué está mal**: La conexión `sqlite3.connect(DB)` se abre en cada request y **nunca se cierra**. No hay un bloque `try/finally` o `with` para garantizar `conn.close()`.

**Por qué importa**: En producción con tráfico real (1.8M cotizaciones/mes en Agentemotor), cada request abre una conexión que nunca se libera. SQLite tiene un límite práctico de conexiones simultáneas. Eventualmente la base de datos se bloquea y todas las peticiones subsecuentes fallan con `SQLITE_BUSY`.

**Qué comportamiento de negocio se rompe**: María y los otros 499 asesores no podrían acceder al sistema. Las renovaciones no se procesarían, los clientes no serían contactados, y las pólizas vencerían sin gestión. **5-10 clientes perdidos al mes** por asesor se convertirían en decenas o cientos.

**Cómo arreglarlo**:
```python
def get_db():
    conn = sqlite3.connect(DB)
    try:
        yield conn
    finally:
        conn.close()

# Usar with statement para garantizar cierre
with get_db() as conn:
    cursor = conn.cursor()
    cursor.execute(...)
```

O mejor, usar un pool de conexiones administrado por Flask (`g` object) o un ORM.

---

## Problema 2: Lógica de negocio incorrecta — la ventana de 30 días no existe

**Línea 27**: `priority = 'urgent' if days_overdue > 7 else 'normal'`

**Línea 29**: `'recommended_action': 'Contactar urgentemente para evitar pérdida del cliente'`

**Qué está mal**: La prioridad y la acción recomendada ignoran completamente la ventana regulatoria de 30 días. Una póliza vencida hace 8 días tiene la misma prioridad "urgent" que una vencida hace 29 días. Una póliza vencida hace 31 días también es "urgent". Además, la acción recomendada es genérica para todas las pólizas vencidas, sin distinguir entre las que aún pueden renovarse (≤30 días) y las que se consideran pérdida (>30 días).

**Por qué importa**: El negocio de Agentemotor depende de que los asesores prioricen correctamente. Si María recibe la misma alerta para una póliza vencida hace 3 días (recuperable) y para una vencida hace 35 días (probablemente perdida), **no puede priorizar**. Las pólizas dentro de la ventana de 30 días son las que generan ingresos por renovación; las de más de 30 días requieren una estrategia completamente diferente (nueva contratación).

**Qué comportamiento de negocio se rompe**: María pierde clientes recuperables porque dedica tiempo a pólizas que ya no se pueden renovar. O peor, trata de renovar pólizas fuera de la ventana como si estuvieran dentro, generando errores operativos con las aseguradoras.

**Cómo arreglarlo**:
```python
RENEWAL_WINDOW_DAYS = 30
URGENT_THRESHOLD = 7

if days_overdue > RENEWAL_WINDOW_DAYS:
    priority = 'lost'
    action = 'Cliente fuera de ventana de renovación. Contactar para nueva contratación.'
elif days_overdue > URGENT_THRESHOLD:
    priority = 'urgent'
    days_left = RENEWAL_WINDOW_DAYS - days_overdue
    action = f'Contactar urgentemente. Ventana de renovación: {days_left} días restantes.'
elif days_overdue > 0:
    priority = 'high'
    action = 'Gestionar renovación dentro de la ventana de 30 días.'
else:
    priority = 'normal'
    action = 'Póliza vigente. Monitorear vencimiento.'
```

---

## Problema 3: Sin manejo de errores — cualquier fallo es un 500 silencioso

**Línea**: Todo el cuerpo de la función.

**Qué está mal**: Si un cliente no existe (base de datos inconsistente), si la conexión falla, si el formato de fecha es inválido en la BD, o si cualquier excepción ocurre, Flask responde con un 500 Internal Server Error sin mensaje útil. En particular:
- `exp_date = datetime.strptime(exp_date_str, '%Y-%m-%d').date()` asume que la fecha siempre tiene el formato correcto
- `client = cursor.fetchone()` y luego `client[0]` asume que el cliente siempre existe — si un client_id referencia un cliente eliminado, esto lanza `TypeError`

**Por qué importa**: En producción, los datos corruptos existen. Una migración mal hecha, un bug en otro módulo, o una inserción manual pueden generar fechas en formato incorrecto o referencias huérfanas. Sin manejo de errores, **María ve una pantalla en blanco** y no sabe si el problema es suyo, del sistema, o de los datos. La asesora no puede trabajar y no hay forma de diagnosticar sin revisar logs del servidor.

**Qué comportamiento de negocio se rompe**: María deja de usar el sistema porque "se cae" sin razón aparente. Vuelve a su Excel. El sistema implementado para reemplazar el Excel fracasa.

**Cómo arreglarlo**:
```python
try:
    conn = sqlite3.connect(DB)
    cursor = conn.cursor()
    # ... operaciones ...
except sqlite3.DatabaseError as e:
    app.logger.error(f"Database error for advisor {advisor_id}: {e}")
    return jsonify({'error': 'Error de base de datos', 'code': 'DB_ERROR'}), 500
except ValueError as e:
    app.logger.error(f"Data integrity error: {e}")
    return jsonify({'error': 'Error en formato de datos', 'code': 'DATA_ERROR'}), 500
finally:
    if conn:
        conn.close()
```

---

## Problema 4: N+1 queries — cada póliza genera 2 queries adicionales

**Líneas 9-27**: Por cada póliza vencida, se ejecutan:
1. Query principal (línea 12): lista las pólizas vencidas
2. Query por póliza (línea 19-20): obtiene datos del cliente
3. Query por póliza (línea 21-23): cuenta intentos de contacto

**Qué está mal**: Si María tiene 100 pólizas vencidas, este endpoint ejecuta 1 + 100 + 100 = **201 queries a la base de datos**. Esto es el clásico problema N+1.

**Por qué importa**: En la escala de Agentemotor (1.8M cotizaciones/mes), este endpoint es llamado frecuentemente. 201 queries por request → latencia alta → timeout del lado del frontend → María ve una pantalla de carga infinita. Con asesores consultando simultáneamente, la base de datos se satura.

**Qué comportamiento de negocio se rompe**: El dashboard tarda demasiado en cargar. María pierde tiempo esperando. En el horario pico (lunes por la mañana, cuando todos los asesores revisan sus carteras), el sistema es inusable.

**Cómo arreglarlo**:
```python
# Usar JOINs para obtener todo en una sola query
cursor.execute("""
    SELECT p.id, p.client_id, p.insurer, p.expiration_date, p.status,
           c.name, c.phone,
           (SELECT COUNT(*) FROM contact_attempts ca WHERE ca.policy_id = p.id) as attempts
    FROM policies p
    JOIN clients c ON c.id = p.client_id
    WHERE p.advisor_id = ? AND p.expiration_date < ?
""", (advisor_id, today))
```

Esto reduce 201 queries a 1.
