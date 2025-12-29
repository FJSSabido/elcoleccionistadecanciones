

function showPlaylistTitle(title) {
    const titleElem = document.getElementById("playlistTitle");
    if (!titleElem || !title) return;

    titleElem.textContent = `🎧 Playlist: ${title}`;
    titleElem.style.display = "block";
}

// =======================
// Renderizar cartas
// =======================
async function renderCards(url) {
    if (!url) return;

    console.log("Intentando generar cartas para URL:", url);

    const cardsContainer = document.getElementById("cards");
    if (!cardsContainer) return;

    cardsContainer.innerHTML = "";

    try {
        const res = await fetch(`/api/cards?url=${encodeURIComponent(url)}`);
        console.log("Respuesta fetch:", res.status);

        if (!res.ok) {
            if (res.status === 401) {
                cardsContainer.innerHTML =
                    "<p class='status error'>Error: Debes conectarte con Spotify para esta playlist (puede ser privada).</p>";
            }
            throw new Error("Error fetching cards: " + res.status);
        }

        const cards = await res.json();
        console.log("Cartas recibidas:", cards.length);

        cards.forEach(card => {
            const cardLink = document.createElement("a");
            cardLink.className = "card-link";
            cardLink.href = card.spotifyUrl;
            cardLink.target = "_blank";

            const cardElem = document.createElement("div");
            cardElem.className = "card";

            cardElem.innerHTML = `
                <div class="card-inner">
                    <div class="card-front">
                        <div class="cover-wrapper">
                            <img class="cover" src="${card.imageUrl}" alt="${card.title}">
                        </div>
                        <div class="info">
                            <div class="title">${card.title}</div>
                            <div class="artist">${card.artist}</div>
                            <div class="album">${card.album}</div>
                            <div class="year">${card.year || 'N/A'}</div>
                        </div>
                    </div>
                    <div class="card-back">
                        <div class="qr"></div>
                        <div class="scan">Escanea para abrir en Spotify</div>
                    </div>
                </div>
            `;

            cardLink.appendChild(cardElem);
            cardsContainer.appendChild(cardLink);

            new QRCode(cardElem.querySelector(".qr"), {
                text: card.spotifyUrl,
                width: 128,
                height: 128,
                colorDark: "#000000",
                colorLight: "#ffffff",
                correctLevel: QRCode.CorrectLevel.H
            });
        });
    } catch (e) {
        console.error("Error rendering cards:", e);
        cardsContainer.innerHTML =
            "<p class='status error'>Error al generar cartas: " + e.message + "</p>";
    }
}

// =======================
// URL compartible
// =======================
function updateShareableUrl(userId, playlistId) {
    const newUrl = `${window.location.origin}${window.location.pathname}?user=${encodeURIComponent(userId)}&playlist=${encodeURIComponent(playlistId)}`;
    history.pushState({ userId, playlistId }, '', newUrl);
}

function extractPlaylistId(playlistUrl) {
    const parts = playlistUrl.split('/playlist/');
    return parts.length > 1 ? parts[1].split('?')[0] : null;
}

function extractUserId(profileUrl) {
    const parts = profileUrl.split('/user/');
    return parts.length > 1 ? parts[1].split('?')[0] : null;
}

// =======================
// Playlists de amigo
// =======================
async function loadFriendPlaylists(profileUrl, targetPlaylistId = null) {
    const status = document.getElementById("friendStatus");
    const select = document.getElementById("friendPlaylistSelect");

    if (!status || !select) return;

    if (!profileUrl) {
        status.textContent = "Introduce la URL del perfil de Spotify";
        status.className = "status error";
        return;
    }

    status.textContent = "Cargando playlists públicas...";
    status.className = "status";
    select.style.display = "none";
    select.innerHTML = "";

    try {
        const res = await fetch(`/api/users/playlists?profileUrl=${encodeURIComponent(profileUrl)}`);
        if (!res.ok) throw new Error();

        const playlists = await res.json();

        playlists.forEach(p => {
            const option = document.createElement("option");
            option.value = `https://open.spotify.com/playlist/${p.id}`;
            option.textContent = `${p.name} (${p.totalTracks} canciones)`;
            select.appendChild(option);
        });

        select.style.display = "block";
        status.textContent = "Playlists públicas cargadas";
        status.className = "status success";

        if (targetPlaylistId) {
            const playlistUrl = `https://open.spotify.com/playlist/${targetPlaylistId}`;
            select.value = playlistUrl;

            if (select.value) {
                const selectedOption = select.selectedOptions[0];
                if (selectedOption) {
                    showPlaylistTitle(selectedOption.textContent);
                }
                renderCards(playlistUrl);
            } else {
                status.textContent = "La playlist no se encontró";
                status.className = "status error";
            }
        }
    } catch {
        status.textContent = "No se pudieron cargar playlists públicas";
        status.className = "status error";
    }
}

// =======================
// Event listeners DEFENSIVOS
// =======================

// Botón playlists amigo
const loadFriendBtn = document.getElementById("loadFriendPlaylistsBtn");
if (loadFriendBtn) {
    loadFriendBtn.addEventListener("click", async () => {
        const input = document.getElementById("friendUsername");
        if (!input) return;
        await loadFriendPlaylists(input.value.trim());
    });
}

// Botón mis playlists
const loadMyPlaylistsBtn = document.getElementById("loadMyPlaylistsBtn");
if (loadMyPlaylistsBtn) {
    loadMyPlaylistsBtn.addEventListener("click", async () => {
        const status = document.getElementById("playlistStatus");
        const select = document.getElementById("playlistSelect");

        if (!status || !select) return;

        status.textContent = "Cargando tus playlists...";
        status.className = "status";
        select.style.display = "none";
        select.innerHTML = "";

        try {
            const res = await fetch(`/api/my/playlists`);
            if (!res.ok) throw new Error();

            const playlists = await res.json();

            playlists.forEach(p => {
                const option = document.createElement("option");
                option.value = `https://open.spotify.com/playlist/${p.id}`;
                option.textContent = `${p.name} (${p.totalTracks} canciones)`;
                select.appendChild(option);
            });

            select.style.display = "block";
            status.textContent = "Tus playlists cargadas";
            status.className = "status success";
        } catch {
            status.textContent = "No se pudieron cargar tus playlists";
            status.className = "status error";
        }
    });
}

// Select playlist propia
const playlistSelect = document.getElementById("playlistSelect");
if (playlistSelect) {
    playlistSelect.addEventListener("change", e => {
        renderCards(e.target.value);
    });
}

// Select playlist amigo
const friendPlaylistSelect = document.getElementById("friendPlaylistSelect");
if (friendPlaylistSelect) {
    friendPlaylistSelect.addEventListener("change", e => {
        const playlistUrl = e.target.value;
        const selectedOption = e.target.selectedOptions[0];
        if (selectedOption) {
            showPlaylistTitle(selectedOption.textContent);
        }
        renderCards(playlistUrl);
        const input = document.getElementById("friendUsername");
        if (!input) return;

        const userId = extractUserId(input.value.trim());
        const playlistId = extractPlaylistId(playlistUrl);

        if (userId && playlistId) {
            updateShareableUrl(userId, playlistId);
        }
    });
}

// Enlace manual
const loadBtn = document.getElementById("loadBtn");
if (loadBtn) {
    loadBtn.addEventListener("click", () => {
        const input = document.getElementById("spotifyUrl");
        if (input) renderCards(input.value.trim());
    });
}

// =======================
// Init desde URL compartida
// =======================
window.addEventListener("load", () => {
    const params = new URLSearchParams(window.location.search);
    if (params.has("user") && params.has("playlist")) {
        const userId = params.get("user");
        const playlistId = params.get("playlist");
        const profileUrl = `https://open.spotify.com/user/${userId}`;

        const input = document.getElementById("friendUsername");
        if (input) input.value = profileUrl;

        loadFriendPlaylists(profileUrl, playlistId);
    }
});
