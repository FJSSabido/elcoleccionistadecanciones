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
    status.className = 'status';

    try {
        const res = await fetch('/api/me/playlists');
        if (res.status === 401) {
            status.textContent = '❌ Debes conectar con Spotify primero';
            status.classList.add('error');
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
        status.classList.add('success');

        select.onchange = () => {
            if (select.value) {
                loadCardsFromPlaylistId(select.value);
            }
        };

    } catch (e) {
        status.textContent = '❌ Error al cargar playlists';
        status.classList.add('error');
        console.error(e);
        select.style.display = 'none';
    }
});

// === CARGAR PLAYLISTS DE UN AMIGO ===
document.getElementById('loadFriendPlaylistsBtn').addEventListener('click', async () => {
    const username = document.getElementById('friendUsername').value.trim();
    if (!username) {
        alert('Introduce un nombre de usuario Spotify');
        return;
    }

    const select = document.getElementById('friendPlaylistSelect');
    const status = document.getElementById('friendStatus');

    select.style.display = 'none';
    select.innerHTML = '<option value="">Cargando...</option>';
    status.textContent = `Cargando playlists públicas de ${username}...`;
    status.className = 'status';

    try {
        const res = await fetch(`/api/users/${encodeURIComponent(username)}/playlists`);

        if (res.status === 404) {
            status.textContent = '❌ Usuario no encontrado o sin playlists públicas';
            status.classList.add('error');
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
        status.classList.add('success');

        select.onchange = async () => {
            if (select.value) {
                let isAuthenticated = false;
                try {
                    const checkRes = await fetch('/api/me/playlists', { method: 'HEAD' });
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
        status.classList.add('error');
        console.error(e);
        select.style.display = 'none';
    }
});

// === FUNCIONES PARA CARGAR CARTAS ===
async function loadCardsFromUrl(url) {
    await generateCards('/api/cards?url=' + encodeURIComponent(url));
}

async function loadCardsFromPlaylistId(playlistId) {
    await generateCards('/api/cards?url=https://open.spotify.com/playlist/' + playlistId);
}

async function generateCards(apiUrl) {
    const cardsContainer = document.getElementById('cards');
    cardsContainer.innerHTML = '<p style="text-align:center; font-size:1.2rem; color:#666;">Cargando cartas...</p>';

    try {
        const res = await fetch(apiUrl);
        if (!res.ok) {
            if (res.status === 401) {
                cardsContainer.innerHTML = '<p style="color:#e74c3c; text-align:center;">❌ Debes conectar con Spotify para acceder a playlists privadas.</p>';
                return;
            }
            throw new Error('Error en la respuesta');
        }

        const cards = await res.json();

        if (cards.length === 0) {
            cardsContainer.innerHTML = '<p style="text-align:center;">No se encontraron canciones.</p>';
            return;
        }

        cardsContainer.innerHTML = ''; // Limpiar mensaje de carga

        cards.forEach(card => {
            const cardElement = document.createElement('div');
            cardElement.className = 'card';
            cardElement.innerHTML = `
                <a href="${card.spotifyUrl}" target="_blank" class="card-link">
                    <div class="card-inner">
                        <div class="card-front">
                            <div class="cover-wrapper">
                                <img src="${card.coverUrl}" alt="Portada" class="cover">
                            </div>
                            <div class="info">
                                <div class="title">${card.title}</div>
                                <div class="artist">${card.artist}</div>
                                <div class="album">${card.album}</div>
                                <div class="year">${card.releaseYear}</div>
                                <div class="duration">${card.duration}</div>
                            </div>
                        </div>
                        <div class="card-back">
                            <div class="qr">
                                <img src="https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(card.spotifyUrl)}" alt="QR">
                            </div>
                            <div class="scan">Escanea para escuchar</div>
                        </div>
                    </div>
                </a>
            `;
            cardsContainer.appendChild(cardElement);
        });

    } catch (e) {
        cardsContainer.innerHTML = '<p style="color: #1DB954; text-align: center;">❌ Error de conexión o username inválido (prueba todo en minúsculas)</p>';
        console.error(e);
    }
}