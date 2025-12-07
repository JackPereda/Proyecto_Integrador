// Funcionalidad para el formulario de artículos
document.addEventListener('DOMContentLoaded', function() {
    const inventarioInicial = document.getElementById('inventarioInicial');
    const entradas = document.getElementById('entradas');
    const salidas = document.getElementById('salidas');
    const inventarioFinal = document.getElementById('inventarioFinal');
    const formulario = document.querySelector('form');

    function calcularInventarioFinal() {
        const inicial = parseFloat(inventarioInicial?.value) || 0;
        const ent = parseFloat(entradas?.value) || 0;
        const sal = parseFloat(salidas?.value) || 0;
        const final = inicial + ent - sal;

        if (inventarioFinal) {
            inventarioFinal.value = final;
        }
    }

    // Prevenir múltiples envíos del formulario
    if (formulario) {
        formulario.addEventListener('submit', function(e) {
            const botonSubmit = this.querySelector('button[type="submit"]');
            if (botonSubmit) {
                botonSubmit.disabled = true;
                botonSubmit.innerHTML = '<ion-icon name="hourglass-outline"></ion-icon> Guardando...';
            }
        });
    }

    // Event listeners para calcular inventario
    if (inventarioInicial) {
        inventarioInicial.addEventListener('input', calcularInventarioFinal);
    }
    if (entradas) {
        entradas.addEventListener('input', calcularInventarioFinal);
    }
    if (salidas) {
        salidas.addEventListener('input', calcularInventarioFinal);
    }

    // Calcular inicialmente
    calcularInventarioFinal();
});

// Funciones de confirmación y eliminación
document.addEventListener('DOMContentLoaded', function() {
    // Confirmación para eliminar
    const linksEliminar = document.querySelectorAll('a.btn-eliminar, button.btn-eliminar');
    linksEliminar.forEach(link => {
        link.addEventListener('click', function(e) {
            if (!confirm('¿Está seguro de eliminar este artículo?')) {
                e.preventDefault();
                return false;
            }

            // Si es un enlace normal, dejar que proceda
            if (this.tagName === 'A') {
                const originalText = this.innerHTML;
                this.innerHTML = '<ion-icon name="hourglass-outline"></ion-icon> Eliminando...';
                this.style.pointerEvents = 'none';
            }
        });
    });

    // Prevenir múltiples clics en botones
    const botones = document.querySelectorAll('.btn-primary, .btn-danger, .btn-editar');
    botones.forEach(boton => {
        boton.addEventListener('click', function(e) {
            if (this.disabled) {
                e.preventDefault();
                return;
            }

            if (this.type === 'submit' || this.classList.contains('btn-danger') || this.classList.contains('btn-primary')) {
                this.disabled = true;
                setTimeout(() => {
                    this.disabled = false;
                }, 2000);
            }
        });
    });
});

// Funcionalidad para la navegación y UI
document.addEventListener('DOMContentLoaded', function() {
    // Activar elemento actual en el menú
    const currentPath = window.location.pathname;
    const menuLinks = document.querySelectorAll('.sideBar a');

    menuLinks.forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // Centrar automáticamente todas las tablas
    centrarTablas();
});

// Función para centrar tablas
function centrarTablas() {
    const tablas = document.querySelectorAll('table');
    tablas.forEach(tabla => {
        // Crear contenedor si no existe
        if (!tabla.parentElement.classList.contains('table-container')) {
            const contenedor = document.createElement('div');
            contenedor.className = 'table-container';
            tabla.parentNode.insertBefore(contenedor, tabla);
            contenedor.appendChild(tabla);
        }

        // Forzar centrado de todas las celdas
        const celdas = tabla.querySelectorAll('td, th');
        celdas.forEach(celda => {
            celda.style.textAlign = 'center';
            celda.style.verticalAlign = 'middle';
        });
    });
}

// Funcionalidad adicional para movimientos
document.addEventListener('DOMContentLoaded', function() {
    const formularioMovimiento = document.querySelector('form');

    if (formularioMovimiento) {
        formularioMovimiento.addEventListener('submit', function(e) {
            const cantidad = document.getElementById('cantidad');
            const tipoMovimiento = document.getElementById('tipoMovimiento');
            const articuloId = document.getElementById('articuloId');

            // Validaciones básicas
            if (articuloId && !articuloId.value) {
                alert('Por favor seleccione un artículo');
                e.preventDefault();
                return;
            }

            if (tipoMovimiento && !tipoMovimiento.value) {
                alert('Por favor seleccione el tipo de movimiento');
                e.preventDefault();
                return;
            }

            if (cantidad && (!cantidad.value || cantidad.value < 1)) {
                alert('Por favor ingrese una cantidad válida');
                e.preventDefault();
                return;
            }

            // Confirmación para salidas
            if (tipoMovimiento && tipoMovimiento.value === 'SALIDA') {
                if (!confirm('¿Está seguro de registrar una salida de inventario?')) {
                    e.preventDefault();
                    return;
                }
            }

            // Loading state
            const botonSubmit = this.querySelector('button[type="submit"]');
            if (botonSubmit) {
                botonSubmit.disabled = true;
                botonSubmit.innerHTML = '<ion-icon name="hourglass-outline"></ion-icon> Procesando...';
            }
        });
    }
});

// Función para mostrar mensajes (si usas AJAX)
function mostrarMensaje(mensaje, tipo) {
    const divMensaje = document.createElement('div');
    divMensaje.className = tipo === 'exito' ? 'mensaje-exito' : 'mensaje-error';
    divMensaje.innerHTML = `
        <ion-icon name="${tipo === 'exito' ? 'checkmark-circle' : 'alert-circle'}"></ion-icon>
        ${mensaje}
    `;

    const contenido = document.querySelector('.contenido');
    contenido.insertBefore(divMensaje, contenido.firstChild);

    setTimeout(() => {
        divMensaje.remove();
    }, 5000);
}