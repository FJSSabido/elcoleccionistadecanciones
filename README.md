[WIP]
# 🎵 El Coleccionista de Canciones

![Banner del proyecto](https://i.imgur.com/EXAMPLE.jpg)
*(Reemplaza el enlace de arriba con una captura bonita de tus cartas generadas cuando la tengas)*

**¡Convierte tus playlists de Spotify en un mazo de cartas coleccionables físicas!**

Este proyecto te permite generar cartas estilo cromo (63×88 mm, tamaño estándar de carta de juego) con la portada, título, artista, álbum, año y duración de cada canción.  
En la parte trasera lleva un **código QR** que enlaza directamente a la canción en Spotify.  
Perfecto para imprimir, coleccionar, regalar o montar tu propia "colección física" de música.

### ✨ Características principales

- Soporta **playlists públicas** sin necesidad de login
- Soporta **playlists privadas y colaborativas** conectando tu cuenta Spotify (OAuth seguro)
- Carga tus propias playlists o las de cualquier usuario público
- Genera cartas con diseño flip (anverso con info bonita, reverso con QR grande)
- Diseño optimizado para **impresión real** (63 × 88 mm, márgenes correctos, colores de fondo oscuro elegantes)
- Totalmente local: corre en tu máquina con Spring Boot + frontend estático

### 🎴 Vista previa de las cartas

<div align="center">
  <img src="https://i.imgur.com/EXAMPLE_FRONT.jpg" width="300" alt="Anverso de carta">
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://i.imgur.com/EXAMPLE_BACK.jpg" width="300" alt="Reverso con QR">
</div>

*(Sube capturas reales de tus cartas generadas y pon los enlaces aquí)*

### 🚀 Cómo usarlo (local)

1. Clona el repositorio
```bash
git clone https://github.com/tu-usuario/elcoleccionistadecanciones.git
cd elcoleccionistadecanciones

Configura tus credenciales de Spotify en src/main/resources/application.properties:

propertiesspotify.client-id=TU_CLIENT_ID
spotify.client-secret=TU_CLIENT_SECRET
Cómo obtenerlas: Crea una app en https://developer.spotify.com/dashboard/

Ejecuta la aplicación

Bash./mvnw spring-boot:run

Abre http://localhost:8080
Pega una playlist, conecta con Spotify si quieres acceder a privadas, ¡y genera tus cartas!

🌍 Despliegue en vivo
¡El proyecto ya está desplegado y listo para usar por cualquiera!
🔗 https://elcoleccionistadecanciones.onrender.com
(Actualiza este enlace cuando lo tengas desplegado)
🖨️ Consejos para imprimir

Usa papel fotográfico o cartulina de 250-300 g/m²
Imprime a tamaño real (100%, sin escalar)
Corte perfecto con guillotina o cizalla para un acabado profesional
Opcional: encájalo en fundas de cartas (standard size) para protegerlas

🤝 Contribuciones
¡Son bienvenidas! Si quieres añadir:

Soporte para álbumes completos
Filtros (solo canciones de cierto año, artista, etc.)
Modo "colección única" (sin duplicados)
Temas alternativos de carta

¡Abre un issue o pull request!
📝 Licencia
MIT License – siéntete libre de usar, modificar y compartir.

Hecho con ❤️ y nostalgia por un fan de la música y las cartas coleccionables
