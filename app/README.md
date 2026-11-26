# ModoGuardian

MVP Android de monitoreo con cámaras, eventos, alertas y perfiles de prueba

## Ejecutar

1. Abre esta carpeta en Android Studio
2. Espera a que termine Gradle Sync
3. Inicia un emulador desde Device Manager
4. Selecciona la configuración `app`
5. Pulsa Run

## Iniciar sesión con perfiles de prueba

Estas cuentas son ficticias y se validan localmente en la app

| Perfil | Correo | Contraseña |
| --- | --- | --- |
| Admin | admin@guardian.test | 123456 |
| Supervisor | supervisor@guardian.test | 123456 |
| Operador | operador@guardian.test | 123456 |

Para probar otro perfil, cierra sesión desde Perfil e ingresa con otra cuenta

## Probar inicio de sesión

1. Deja los campos vacíos y pulsa Ingresar al sistema
2. Comprueba que aparezcan los mensajes de validación
3. Escribe un correo con formato inválido y vuelve a pulsar
4. Cambia entre Registro e Inicio de sesión y confirma que los campos se limpian
5. Prueba las tres cuentas ficticias y revisa el rol en Perfil
6. Ingresa una cuenta y una contraseña incorrecta para probar el mensaje de error

## Probar registro

1. Pulsa Regístrate
2. Comprueba que aparezcan correo, contraseña y confirmar contraseña
3. Envía el formulario vacío y revisa los mensajes
4. Escribe contraseñas distintas y confirma que no se registre
5. Escribe un correo válido y repite la contraseña
6. El registro requiere que Correo/contraseña esté habilitado

## Google

1. Pulsa el boton continuar con Google, ya sea en inicio de sesión o en el regsitro.
2. Selecciona o añade una cuenta de Google.
3. Ingresa tu correo y contraseña de Google.
4. Acepta las condiciones -> **I agree**
5. Se deberia mostrar el panel.

## Datos del MVP

Cámaras, eventos y alertas usan datos simulados