
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

    let prefix = isTrack ? "🎵 Canción" : "🎧 Playlist";
    let suffix = "";

    if (typeof totalTracks === "number") {
        suffix = ` (${totalTracks} ${totalTracks === 1 ? "canción" : "canciones"})`;
    }

    titleElem.textContent = `${prefix}: ${title}${suffix}`;
    titleElem.style.display = "block";
}

/*====================================
    Función para resetear la vista.
======================================*/
function resetCardsView() {
    const titleElem = document.getElementById("playlistTitle");
    const cardsContainer = document.getElementById("cards");
    const pagination = document.getElementById("pagination");

    if (titleElem) {
        titleElem.textContent = "";
        titleElem.style.display = "none";
    }

    if (cardsContainer) {
        cardsContainer.innerHTML = "";
    }

    if (pagination) {
        pagination.style.display = "none";
    }

    allCards = [];
    currentPage = 1;
}

// =======================
// Renderizar cartas
// =======================
async function renderCards(url) {
    if (!url) return;

    updateUrlWithSpotifyUrl(url); // 👈 URL SIEMPRE ACTUALIZADA

    console.log("Intentando generar cartas para URL:", url);

    const cardsContainer = document.getElementById("cards");
    if (!cardsContainer) return;

    cardsContainer.innerHTML = "";

    const manualStatus = document.getElementById("manualStatus");
    if (manualStatus) {
        manualStatus.textContent = "";
        manualStatus.className = "status";
    }

    showLoadingPopup(); // 👈 MOSTRAR POPUP AL EMPEZAR

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

        // 👇 TÍTULO REAL DE PLAYLIST O CANCIÓN (Spotify oficial)
        const spotifyTitle = await fetchSpotifyTitle(url);
        if (spotifyTitle) {
            const isTrack = cards.length === 1;
            showPlaylistTitle(spotifyTitle, cards.length, isTrack);
        }


        allCards = cards;
        currentPage = 1;
        renderPage(currentPage);

        // 👇 TODO HA TERMINADO
        hideLoadingPopup();
        scrollToCards();

    } catch (e) {
          console.error("Error rendering cards:", e);
          hideLoadingPopup();

          resetCardsView(); // 👈 LIMPIA título, cartas y paginación

          const manualStatus = document.getElementById("manualStatus");
          if (manualStatus) {
              manualStatus.textContent = "Error al generar cartas. Revisa el enlace de Spotify.";
              manualStatus.className = "status error";
          }
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

function updateUrlWithSpotifyUrl(spotifyUrl) {
    const newUrl = `${window.location.origin}${window.location.pathname}?spotifyUrl=${encodeURIComponent(spotifyUrl)}`;
    history.pushState({ spotifyUrl }, '', newUrl);
}

function getSpotifyUrlFromLocation() {
    const params = new URLSearchParams(window.location.search);
    return params.get("spotifyUrl");
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
    });
}


// =======================
// Init desde URL compartida
// =======================
window.addEventListener("load", () => {
    const spotifyUrl = getSpotifyUrlFromLocation();
    if (spotifyUrl) {
        renderCards(spotifyUrl);
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


