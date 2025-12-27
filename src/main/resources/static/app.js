// Función reutilizable para renderizar cartas
async function renderCards(url) {
    if (!url) return;

    console.log("Intentando generar cartas para URL:", url); // Log para debug

    const cardsContainer = document.getElementById("cards");
    cardsContainer.innerHTML = ""; // Limpiar previas

    try {
        const res = await fetch(`/api/cards?url=${encodeURIComponent(url)}`);
        console.log("Respuesta fetch:", res.status); // Log status
        if (!res.ok) {
            if (res.status === 401) {
                cardsContainer.innerHTML = "<p class='status error'>Error: Debes conectarte con Spotify para esta playlist (puede ser privada).</p>";
            }
            throw new Error("Error fetching cards: " + res.status);
        }

        const cards = await res.json();
        console.log("Cartas recibidas:", cards.length); // Log cantidad

        cards.forEach(card => {
            const cardLink = document.createElement("a");
            cardLink.className = "card-link";
            cardLink.href = card.spotifyUrl;
            cardLink.target = "_blank"; // Abre en nueva pestaña

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
                            <div class="duration">${card.duration || 'N/A'}</div>
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

            // Generar QR en el .qr
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
        cardsContainer.innerHTML = "<p class='status error'>Error al generar cartas: " + e.message + "</p>";
    }
}

// Cargar playlists de amigo
document.getElementById("loadFriendPlaylistsBtn").addEventListener("click", async () => {
    const profileUrl = document.getElementById("friendUsername").value.trim();
    const status = document.getElementById("friendStatus");
    const select = document.getElementById("friendPlaylistSelect");

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
    } catch (e) {
        status.textContent = "No se pudieron cargar playlists públicas";
        status.className = "status error";
    }
});

// Cargar mis playlists
document.getElementById("loadMyPlaylistsBtn").addEventListener("click", async () => {
    const status = document.getElementById("playlistStatus");
    const select = document.getElementById("playlistSelect");

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
    } catch (e) {
        status.textContent = "No se pudieron cargar tus playlists (¿Conectado a Spotify?)";
        status.className = "status error";
    }
});

// Render al seleccionar playlist (mía)
document.getElementById("playlistSelect").addEventListener("change", (e) => {
    renderCards(e.target.value);
});

// Render al seleccionar playlist (amigo)
document.getElementById("friendPlaylistSelect").addEventListener("change", (e) => {
    renderCards(e.target.value);
});

// Render desde enlace manual
document.getElementById("loadBtn").addEventListener("click", () => {
    const url = document.getElementById("spotifyUrl").value.trim();
    renderCards(url);
});