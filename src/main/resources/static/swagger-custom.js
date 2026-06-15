(function () {
    function addBackButton() {
        var topbar = document.querySelector('.swagger-ui .topbar');
        if (!topbar) {
            topbar = document.querySelector('.topbar');
        }
        if (!topbar) { setTimeout(addBackButton, 200); return; }

        var btn = document.createElement('a');
        btn.href = '/';
        btn.innerHTML = '\u2190 Volver al panel';
        btn.title = 'Volver al panel de gesti\u00f3n';

        var styles = {
            position: 'absolute',
            right: '20px',
            top: '50%',
            transform: 'translateY(-50%)',
            color: 'rgba(255,255,255,0.8)',
            textDecoration: 'none',
            fontSize: '14px',
            padding: '6px 14px',
            border: '1px solid rgba(255,255,255,0.3)',
            borderRadius: '6px',
            transition: 'all 0.2s',
            whiteSpace: 'nowrap'
        };

        Object.assign(btn.style, styles);

        btn.onmouseover = function () {
            this.style.color = 'white';
            this.style.borderColor = 'rgba(255,255,255,0.7)';
            this.style.background = 'rgba(255,255,255,0.1)';
        };

        btn.onmouseout = function () {
            this.style.color = 'rgba(255,255,255,0.8)';
            this.style.borderColor = 'rgba(255,255,255,0.3)';
            this.style.background = 'transparent';
        };

        topbar.style.position = 'relative';
        topbar.appendChild(btn);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', addBackButton);
    } else {
        addBackButton();
    }
})();
