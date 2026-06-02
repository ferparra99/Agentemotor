-- Seed data for Agentemotor
-- Advisor
INSERT OR IGNORE INTO advisors (id, name, email, phone) VALUES (1, 'María Asesora', 'maria@agentemotor.co', '+57 300 123 4567');

-- Clients
INSERT OR IGNORE INTO clients (id, name, phone, email, notes, advisor_id) VALUES
(1, 'Carlos Méndez', '+57 310 111 2233', 'carlos@email.com', 'Cliente desde 2019. Paga puntual.', 1),
(2, 'Ana Lucía Rojas', '+57 320 222 3344', 'ana@email.com', 'Prefiere WhatsApp.', 1),
(3, 'Pedro Infante', '+57 300 333 4455', 'pedro@email.com', 'Tiene 2 autos asegurados.', 1),
(4, 'Laura Jiménez', '+57 315 444 5566', 'laura@email.com', 'Renovó el mes pasado.', 1),
(5, 'Jorge Velásquez', '+57 301 555 6677', 'jorge@email.com', 'Cliente nuevo. Sensible al precio.', 1),
(6, 'Diana Moreno', '+57 318 666 7788', 'diana@email.com', NULL, 1),
(7, 'Ricardo Bustos', '+57 311 777 8899', 'ricardo@email.com', 'Asegura flota de 3 vehículos.', 1),
(8, 'Sofía Torres', '+57 313 888 9900', 'sofia@email.com', NULL, 1);

-- Policies
INSERT OR IGNORE INTO policies (id, policy_number, type, insurer, start_date, expiration_date, status, renewal_count, client_id, advisor_id) VALUES
(1, 'AUTO-001-2025', 'AUTO', 'Seguros Sura', '2025-01-15', DATE('now', '+3 days'), 'ACTIVE', 0, 1, 1);
INSERT OR IGNORE INTO policies (id, policy_number, type, insurer, start_date, expiration_date, status, renewal_count, client_id, advisor_id) VALUES
(2, 'HOGAR-002-2025', 'HOGAR', 'Mapfre', '2025-03-01', DATE('now', '+20 days'), 'ACTIVE', 0, 2, 1);
INSERT OR IGNORE INTO policies (id, policy_number, type, insurer, start_date, expiration_date, status, renewal_count, client_id, advisor_id) VALUES
(3, 'AUTO-003-2025', 'AUTO', 'Allianz', '2025-06-01', DATE('now', '+120 days'), 'ACTIVE', 0, 3, 1);
INSERT OR IGNORE INTO policies (id, policy_number, type, insurer, start_date, expiration_date, status, renewal_count, client_id, advisor_id) VALUES
(4, 'VIDA-004-2025', 'VIDA', 'Sura', '2025-02-01', DATE('now', '+180 days'), 'ACTIVE', 0, 4, 1);
INSERT OR IGNORE INTO policies (id, policy_number, type, insurer, start_date, expiration_date, status, renewal_count, client_id, advisor_id) VALUES
(5, 'AUTO-005-2024', 'AUTO', 'Bolívar', '2024-06-01', DATE('now', '-2 days'), 'ACTIVE', 0, 5, 1);
INSERT OR IGNORE INTO policies (id, policy_number, type, insurer, start_date, expiration_date, status, renewal_count, client_id, advisor_id) VALUES
(6, 'AUTO-006-2024', 'AUTO', 'Sura', '2024-07-01', DATE('now', '-15 days'), 'ACTIVE', 0, 6, 1);
INSERT OR IGNORE INTO policies (id, policy_number, type, insurer, start_date, expiration_date, status, renewal_count, client_id, advisor_id) VALUES
(7, 'AUTO-007-2024', 'AUTO', 'Mapfre', '2024-05-01', DATE('now', '-45 days'), 'ACTIVE', 0, 7, 1);
INSERT OR IGNORE INTO policies (id, policy_number, type, insurer, start_date, expiration_date, status, renewal_count, client_id, advisor_id) VALUES
(8, 'AUTO-008-2023', 'AUTO', 'Sura', '2023-06-01', '2024-06-01', 'RENEWED', 1, 4, 1);

-- Contact attempts
INSERT OR IGNORE INTO contact_attempts (id, date, type, result, notes, policy_id) VALUES
(1, DATE('now', '-14 days', '+2 hours'), 'CALL', 'NO_ANSWER', 'Llamada sin respuesta.', 5),
(2, DATE('now', '-12 days', '+3 hours'), 'WHATSAPP', 'LEFT_MESSAGE', 'Se dejó mensaje de voz.', 5),
(3, DATE('now', '-3 days', '+1 hours'), 'EMAIL', 'NOT_INTERESTED', 'Dijo que está evaluando otras opciones.', 6),
(4, DATE('now', '-90 days'), 'CALL', 'CONTACTED', 'Renovación completada por teléfono.', 8);
