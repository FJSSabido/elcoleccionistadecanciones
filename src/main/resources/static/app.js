// === CARGAR CARTAS DESDE ENLACE MANUAL ===
document.getElementById('loadBtn').addEventListener('click', async () => {
    const url = document.getElementById('spotifyUrl').value.trim();
    if (!url) {
        alert('Pega un enlace de Spotify');
        return;
    }
    await loadCardsFromUrl(url);
});

// === CARGAR MIS PLAYLISTS ===
document.getElementById('loadMyPlaylistsBtn').addEventListener('click', async () => {
    const select = document.getElementById('playlistSelect');
    const status = document.getElementById('playlistStatus');

    select.style.display = 'none';
    select.innerHTML = '<option value="">Cargando...</option>';
    status.textContent = 'Cargando tus playlists...';

    try {
        const res = await fetch('/api/me/playlists');
        if (res.status === 401) {
            status.textContent = '❌ Debes conectar con Spotify primero';
            select.style.display = 'none';
            return;
        }
        if (!res.ok) throw new Error('Error del servidor');

        const playlists = await res.json();
        select.innerHTML = '<option value="">-- Selecciona una playlist --</option>';

        playlists.forEach(pl => {
            const opt = document.createElement('option');
            opt.value = pl.id;
            opt.textContent = `${pl.name} (${pl.trackCount} canciones)`;
            select.appendChild(opt);
        });

        select.style.display = 'block';
        status.textContent = `✅ ${playlists.length} playlists cargadas`;

        // Al seleccionar una playlist → generar cartas
        select.onchange = () => {
            if (select.value) {
                loadCardsFromPlaylistId(select.value);
            }
        };

    } catch (e) {
        status.textContent = '❌ Error al cargar playlists';
        console.error(e);
        select.style.display = 'none';
    }
});

// === FUNCIÓN PARA CARGAR CARTAS DESDE URL ===
async function loadCardsFromUrl(url) {
    await generateCards('/api/cards?url=' + encodeURIComponent(url));
}

// === FUNCIÓN PARA CARGAR CARTAS DESDE ID DE PLAYLIST ===
async function loadCardsFromPlaylistId(playlistId) {
    await generateCards('/api/cards?url=https://open.spotify.com/playlist/' + playlistId);
}

// === FUNCIÓN COMÚN PARA GENERAR Y MOSTRAR CARTAS ===
async function generateCards(apiUrl) {
    let res;
    try {
        res = await fetch(apiUrl);
    } catch (e) {
        alert('No se pudo conectar con el servidor');
        return;
    }

    if (!res.ok) {
        let msg = 'Error procesando la playlist';
        if (res.status === 404) {
            msg = 'Playlist no encontrada o no accesible';
        } else if (res.status === 401) {
            msg = 'Conecta con Spotify para acceder a playlists privadas';
        }
        alert(msg);
        return;
    }

    const cards = await res.json();
    if (!Array.isArray(cards) || cards.length === 0) {
        alert('No se encontraron canciones');
        return;
    }

    const container = document.getElementById('cards');
    container.innerHTML = '';

    cards.forEach(card => {
        const qrUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=' + encodeURIComponent(card.spotifyUrl);

        container.innerHTML += `
          <a href="${card.spotifyUrl}" target="_blank" rel="noopener noreferrer" class="card-link">
            <div class="card">
              <div class="card-inner">
                <div class="card-front">
                  <div class="cover-wrapper">
                    <img class="cover" src="${card.coverUrl}" alt="Portada">
                  </div>
                  <div class="info">
                    <h1 class="title">${card.title}</h1>
                    <p class="artist">${card.artist}</p>
                    <p class="album">${card.album}</p>
                    <p class="year">${card.releaseYear}</p>
                    <p class="duration">${card.duration}</p>
                  </div>
                </div>
                <div class="card-back">
                  <img class="qr" src="${qrUrl}" alt="QR">
                  <p class="scan">Escanea para escuchar</p>
                </div>
              </div>
            </div>
          </a>
        `;
    });
}
// === CARGAR PLAYLISTS DE UN AMIGO ===
document.getElementById('loadFriendPlaylistsBtn').addEventListener('click', async () => {
    const username = document.getElementById('friendUsername').value.trim();
    const select = document.getElementById('friendPlaylistSelect');
    const status = document.getElementById('friendStatus');

    if (!username) {
        alert('Introduce un nombre de usuario de Spotify');
        return;
    }

    select.style.display = 'none';
    select.innerHTML = '<option value="">Cargando...</option>';
    status.textContent = `Cargando playlists públicas de ${username}...`;

    try {
        const res = await fetch(`/api/users/${encodeURIComponent(username)}/playlists`);

        if (res.status === 404) {
            status.textContent = '❌ Usuario no encontrado o sin playlists públicas';
            select.style.display = 'none';
            return;
        }
        if (!res.ok) throw new Error('Error');

        const playlists = await res.json();
        select.innerHTML = '<option value="">-- Selecciona una playlist --</option>';

        playlists.forEach(pl => {
            const opt = document.createElement('option');
            opt.value = pl.id;
            opt.textContent = `${pl.name} (${pl.trackCount} canciones)`;
            select.appendChild(opt);
        });

        select.style.display = 'block';
        status.textContent = `✅ ${playlists.length} playlists públicas encontradas`;

        // Al seleccionar una playlist de un amigo → generar cartas (con comprobación de login)
        select.onchange = async () => {
            if (select.value) {
                // Intentamos una llamada que requiere token de usuario
                let isAuthenticated = false;
                try {
                    const checkRes = await fetch('/api/me/playlists', { method: 'HEAD' }); // HEAD para no cargar datos
                    if (checkRes.ok) {
                        isAuthenticated = true;
                    }
                } catch (e) {
                    // Si falla, probablemente no autenticado
                }

                if (!isAuthenticated) {
                    if (confirm('Para generar cartas de playlists de otros usuarios (incluidas las oficiales de Spotify), debes conectar con tu cuenta Spotify.\n¿Quieres conectar ahora?')) {
                        window.location.href = '/spotify/login';
                    }
                    select.value = ''; // deselecciona
                    return;
                }

                loadCardsFromPlaylistId(select.value);
            }
        };

    } catch (e) {
        if (res && res.status === 404) {
            status.textContent = '❌ Usuario no encontrado o sin playlists públicas';
        } else {
            status.textContent = '❌ Error de conexión o username inválido (prueba todo en minúsculas)';
        }
        console.error(e);
        select.style.display = 'none';
    }
});