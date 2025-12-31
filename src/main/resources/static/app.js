
/*======================================
    Mantener las URL para las playlist.
========================================*/
function updateUrlWithProfile(profileUrl) {
    const params = new URLSearchParams(window.location.search);
    params.set("profile", profileUrl);
    params.delete("spotify"); // evita estados mezclados

    const newUrl =
        window.location.pathname + "?" + params.toString();

    window.history.pushState({}, "", newUrl);
}


/*======================================
    Mantener las URL para las canciones.
========================================*/
function updateUrlWithSpotify(spotifyUrl) {
    const params = new URLSearchParams(window.location.search);

    if (spotifyUrl) {
        params.set("spotify", spotifyUrl);
    } else {
        params.delete("spotify");
    }

    const newUrl =
        window.location.pathname +
        (params.toString() ? "?" + params.toString() : "");

    window.history.pushState({}, "", newUrl);
}

/*===================
    Paginación.
=====================*/
const PAGE_SIZE = 100;
let allCards = [];
let currentPage = 1;

function renderPage(page) {
    const cardsContainer = document.getElementById("cards");
    const pagination = document.getElementById("pagination");
    const pageInfo = document.getElementById("pageInfo");
    const prevBtn = document.getElementById("prevPageBtn");
    const nextBtn = document.getElementById("nextPageBtn");

    if (!cardsContainer || !pagination) return;

    cardsContainer.innerHTML = "";

    const start = (page - 1) * PAGE_SIZE;
    const end = start + PAGE_SIZE;
    const pageCards = allCards.slice(start, end);

    // 👇 Centrar carta si solo hay una en la página
    if (pageCards.length === 1) {
        cardsContainer.classList.add("single-card");
    } else {
        cardsContainer.classList.remove("single-card");
    }


    pageCards.forEach(card => {
        const cardLink = document.createElement("a");
        cardLink.className = "card-link";
        cardLink.href = card.spotifyUrl;
        cardLink.target = "_blank";

        const cardElem = document.createElement("div");
        cardElem.className = "card";

        // NEW: Conditionally add year div only if year exists
        let yearHtml = card.year ? `<div class="year">${card.year}</div>` : '';

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
                        ${yearHtml}
                    </div>
                </div>
                <div class="card-back">
                    <div class="qr"></div>
                </div>
            </div>
        `;

        cardLink.appendChild(cardElem);
        cardsContainer.appendChild(cardLink);

        new QRCode(cardElem.querySelector(".qr"), {
            text: card.spotifyUrl,
            width: 128,
            height: 128,
            correctLevel: QRCode.CorrectLevel.H
        });
    });

    const totalPages = Math.ceil(allCards.length / PAGE_SIZE);

    pageInfo.textContent = `Página ${page} de ${totalPages}`;
    prevBtn.disabled = page === 1;
    nextBtn.disabled = page === totalPages;

    pagination.style.display = totalPages > 1 ? "block" : "none";

    scrollToCards();
}



/*PINTAR EL TÍTULO DE LAS PLAYLISTS*/
async function fetchSpotifyTitle(spotifyUrl) {
    try {
        const res = await fetch(
            `https://open.spotify.com/oembed?url=${encodeURIComponent(spotifyUrl)}`
        );
        if (!res.ok) return null;

        const data = await res.json();
        return data.title || null;
    } catch {
        return null;
    }
}


/*GESTIÓN DEL POPUP DE CARGA*/
function showLoadingPopup() {
    const popup = document.getElementById("loadingPopup");
    if (popup) popup.style.display = "flex";
}

function hideLoadingPopup() {
    const popup = document.getElementById("loadingPopup");
    if (popup) popup.style.display = "none";
}

function scrollToCards() {
    const cards = document.getElementById("cards");
    if (cards) {
        cards.scrollIntoView({ behavior: "smooth", block: "start" });
    }
}


/*MOSTRAR EL TITULO DE LA PLAYLIST*/
function showPlaylistTitle(title, totalTracks = null, isTrack = false) {
    const titleElem = document.getElementById("playlistTitle");
    if (!titleElem || !title) return;

    let prefix = isTrack ? "Canción" : "Playlist";
    let formatted = `${prefix}: ${title}`;

    if (totalTracks !== null && !isTrack) {
        formatted += ` (${totalTracks} canciones)`;
    }

    titleElem.textContent = formatted;
    titleElem.style.display = "block";
}

function getSpotifyUrlFromLocation() {
    const params = new URLSearchParams(window.location.search);
    return params.get("spotify");
}

async function renderCards(spotifyUrl) {
    if (!spotifyUrl) return;

    showLoadingPopup();

    const cardsContainer = document.getElementById("cards");
    if (cardsContainer) cardsContainer.innerHTML = "";

    const statusElems = [
        document.getElementById("manualStatus"),
        document.getElementById("playlistStatus"),
        document.getElementById("friendStatus")
    ].filter(Boolean);

    statusElems.forEach(s => {
        s.textContent = "";
        s.className = "status";
    });

    let playlistTitle = await fetchSpotifyTitle(spotifyUrl);
    let isTrack = spotifyUrl.includes("/track/");

    try {
        const res = await fetch(`/api/cards?url=${encodeURIComponent(spotifyUrl)}`);
        if (!res.ok) throw new Error();

        const data = await res.json();

        allCards = data.map(card => ({
            title: card.title,
            artist: card.artist,
            album: card.album,
            year: card.year,
            imageUrl: card.imageUrl,
            spotifyUrl: card.spotifyUrl
        }));

        renderPage(1);

        showPlaylistTitle(playlistTitle || "Título desconocido", allCards.length, isTrack);

    } catch {
        statusElems.forEach(s => {
            s.textContent = "No se pudieron generar las cartas";
            s.className = "status error";
        });
    } finally {
        hideLoadingPopup();
    }
}

// =======================
// Cargar playlists propias → Generar cartas directamente
// =======================
const loadMyPlaylistsBtn = document.getElementById("loadMyPlaylistsBtn");
if (loadMyPlaylistsBtn) {
    loadMyPlaylistsBtn.addEventListener("click", async () => {
        const status = document.getElementById("playlistStatus");

        if (!status) return;

        status.textContent = "Cargando tus playlists...";
        status.className = "status";

        showLoadingPopup();

        try {
            const res = await fetch(`/api/my/playlists`);
            if (!res.ok) throw new Error();

            const data = await res.json();
            const playlists = data.playlists;
            const displayName = data.displayName || "Tus";

            allCards = playlists.map(p => ({
                title: p.name,
                artist: `By ${p.owner}`,
                album: `${p.totalTracks} tracks`,
                year: '',
                imageUrl: p.imageUrl,
                spotifyUrl: `https://open.spotify.com/playlist/${p.id}`
            }));

            renderPage(1);

            showPlaylistTitle(`${displayName} playlists`, playlists.length);

            status.textContent = "Tus playlists cargadas como cartas";
            status.className = "status success";
        } catch {
            status.textContent = "No se pudieron cargar tus playlists";
            status.className = "status error";
        } finally {
            hideLoadingPopup();
        }
    });
}

// =======================
// Cargar playlists de amigo → Generar cartas directamente
// =======================
const loadFriendPlaylistsBtn = document.getElementById("loadFriendPlaylistsBtn");
if (loadFriendPlaylistsBtn) {
    loadFriendPlaylistsBtn.addEventListener("click", async () => {
        const input = document.getElementById("friendUsername");
        const status = document.getElementById("friendStatus");

        if (!input || !status) return;

        const profileUrl = input.value.trim();

        if (!profileUrl) {
            status.textContent = "Introduce la URL del perfil de Spotify";
            status.className = "status error";
            return;
        }

        status.textContent = "Cargando playlists públicas...";
        status.className = "status";

        showLoadingPopup();

        try {
            const res = await fetch(`/api/users/playlists?profileUrl=${encodeURIComponent(profileUrl)}`);
            if (!res.ok) throw new Error();

            const data = await res.json();
            const playlists = data.playlists;
            const displayName = data.displayName || "el perfil";

            allCards = playlists.map(p => ({
                title: p.name,
                artist: `By ${p.owner}`,
                album: `${p.totalTracks} tracks`,
                year: '',
                imageUrl: p.imageUrl,
                spotifyUrl: `https://open.spotify.com/playlist/${p.id}`
            }));

            renderPage(1);
            showPlaylistTitle(`Playlists públicas de ${displayName}`, playlists.length);
            updateUrlWithProfile(profileUrl);

            status.textContent = "Playlists cargadas como cartas";
            status.className = "status success";
        } catch {
            status.textContent = "No se pudieron cargar las playlists públicas (verifica la URL o si son públicas)";
            status.className = "status error";
        } finally {
            hideLoadingPopup();
        }
    });
}

// Enlace manual
const loadBtn = document.getElementById("loadBtn");
if (loadBtn) {
    loadBtn.addEventListener("click", () => {
        const input = document.getElementById("spotifyUrl");
        const status = document.getElementById("manualStatus");

        if (!input || !status) return;

        const url = input.value.trim();

        if (!url) {
            status.textContent = "Introduce un enlace de Spotify (playlist o canción)";
            status.className = "status error";
            return;
        }

        status.textContent = "";
        status.className = "status";

        renderCards(url);
        updateUrlWithSpotify(url);
    });
}


// =======================
// Init desde URL compartida
// =======================
window.addEventListener("load", () => {
    const params = new URLSearchParams(window.location.search);
    const spotifyUrl = params.get("spotify");
    const profileUrl = params.get("profile");

    if (spotifyUrl) {
        renderCards(spotifyUrl);
    } else if (profileUrl) {
        document.getElementById("friendUsername").value = profileUrl;
        document.getElementById("loadFriendPlaylistsBtn").click();
    }
});



/*==================================
    Necesario para la paginación
====================================*/
document.getElementById("prevPageBtn")?.addEventListener("click", () => {
    if (currentPage > 1) {
        showLoadingPopup();              // 👈 mostrar popup
        currentPage--;

        // pequeño delay para que el popup se vea
        setTimeout(() => {
            renderPage(currentPage);
            hideLoadingPopup();          // 👈 ocultar popup
        }, 150);
    }
});

document.getElementById("nextPageBtn")?.addEventListener("click", () => {
    const totalPages = Math.ceil(allCards.length / PAGE_SIZE);
    if (currentPage < totalPages) {
        showLoadingPopup();              // 👈 mostrar popup
        currentPage++;

        setTimeout(() => {
            renderPage(currentPage);
            hideLoadingPopup();          // 👈 ocultar popup
        }, 150);
    }
});
