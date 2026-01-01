/* Modified: app.js */

// ==========================================
// 1. CONFIGURACIÓN Y VARIABLES GLOBALES
// ==========================================
const PAGE_SIZE = 100;
let allCards = [];
let currentPage = 1;

// ==========================================
// 2. FUNCIONES DE NAVEGACIÓN Y URL
// ==========================================

function updateUrlWithInput(url, type) {
    const params = new URLSearchParams(window.location.search);
    if (type === 'profile') {
        params.set("profile", url);
        params.delete("spotify");
    } else {
        params.set("spotify", url);
        params.delete("profile");
    }
    const newUrl = window.location.pathname + "?" + params.toString();
    window.history.pushState({}, "", newUrl);
}

function updateUrlWithProfile(profileUrl) {
    const params = new URLSearchParams(window.location.search);
    params.set("profile", profileUrl);
    params.delete("spotify");
    const newUrl = window.location.pathname + "?" + params.toString();
    window.history.pushState({}, "", newUrl);
}

function updateUrlWithSpotify(spotifyUrl) {
    const params = new URLSearchParams(window.location.search);
    if (spotifyUrl) {
        params.set("spotify", spotifyUrl);
    } else {
        params.delete("spotify");
    }
    const newUrl = window.location.pathname + (params.toString() ? "?" + params.toString() : "");
    window.history.pushState({}, "", newUrl);
}

// ==========================================
// 3. RENDERIZADO DE CARTAS (PAGINACIÓN)
// ==========================================

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

    // Centrar carta si solo hay una en la página
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

        // FIX: Generar HTML de año y duración solo si existen
        const yearHtml = card.year ? `<div class="year">${card.year}</div>` : '';
        const durationHtml = card.duration ? `<div class="duration">${card.duration}</div>` : '';

        // FIX: Estructura HTML que coincide con tu style.css
        cardElem.innerHTML = `
            <div class="card-inner">
                <div class="card-front">
                    <div class="cover-wrapper">
                        <img class="cover" src="${card.imageUrl}" alt="${card.title}" crossorigin="anonymous">
                    </div>
                    <div class="info">
                        <div class="title">${card.title}</div>
                        <div class="artist">${card.artist}</div>
                        <div class="album">${card.album}</div>
                        ${yearHtml}
                    </div>
                </div>
                <div class="card-back">
                    <p class="scan">www.elcoleccionistadecanciones.com</p>
                    <div class="qr"></div>
                </div>
            </div>
        `;

        cardLink.appendChild(cardElem);
        cardsContainer.appendChild(cardLink);

        // Generar QR
        new QRCode(cardElem.querySelector(".qr"), {
            text: card.spotifyUrl,
            width: 64,
            height: 64,
            correctLevel: QRCode.CorrectLevel.H
        });
    });

    const totalPages = Math.ceil(allCards.length / PAGE_SIZE);
    pageInfo.textContent = `Página ${page} de ${totalPages}`;

    // Control de botones
    if(prevBtn) prevBtn.disabled = page === 1;
    if(nextBtn) nextBtn.disabled = page === totalPages;

    pagination.style.display = totalPages > 1 ? "block" : "none";

    scrollToCards();
}

// ==========================================
// 4. LÓGICA DE CARGA DE DATOS (FETCH)
// ==========================================

async function fetchSpotifyTitle(spotifyUrl) {
    try {
        const res = await fetch(`https://open.spotify.com/oembed?url=$${encodeURIComponent(spotifyUrl)}`);
        if (!res.ok) return null;
        const data = await res.json();
        return data.title || null;
    } catch {
        return null;
    }
}

// Carga principal de cartas (Tracks o Playlists por URL)
async function renderCards(url) {
    const status = document.getElementById("unifiedStatus");
    status.textContent = "";
    status.className = "status";

    showLoadingPopup();

    try {
        const response = await fetch('/api/cards?url=' + encodeURIComponent(url));
        if (!response.ok) {
            throw new Error(await response.text());
        }

        const data = await response.json();
        allCards = data.cards || [];
        currentPage = 1;

        // FIX: Unidad singular/plural
        const unit = allCards.length === 1 ? 'canción' : 'canciones';

        showPlaylistTitle(data.name, allCards.length, unit);
        renderPage(currentPage);
        scrollToCards();
        showCoffeeButton();

        status.className = "status success";
    } catch (error) {
        console.error("Error:", error);
        status.textContent = error.message || "Error al generar las cartas";
        status.className = "status error";
    } finally {
        hideLoadingPopup();
    }
}

// Carga de Playlists de un Amigo (Perfil)
async function loadFriendPlaylists(profileUrl) {
    const status = document.getElementById("unifiedStatus");
    if (!profileUrl) {
        if(status) {
            status.textContent = "Introduce una URL válida";
            status.className = "status error";
        }
        return;
    }

    if(status) {
        status.className = "status";
    }
    showLoadingPopup();

    try {
        const res = await fetch(`/api/users/playlists?profileUrl=${encodeURIComponent(profileUrl)}`);
        if (!res.ok) throw new Error();

        const data = await res.json();
        const playlists = data.playlists;
        const displayName = data.displayName || "el perfil";

        allCards = playlists.map(p => ({
            title: p.name,
            artist: `De ${p.owner}`,
            album: `${p.totalTracks} canciones`,
            year: '',      // Playlists no suelen tener año único
            duration: '',  // Playlists no tienen duración única en la carta resumen
            imageUrl: p.imageUrl,
            spotifyUrl: `https://open.spotify.com/playlist/${p.id}`
        }));

        renderPage(1);
        showCoffeeButton();
        showPlaylistTitle(`Playlists públicas de ${displayName}`, playlists.length, 'playlists');

        if(status) {
            status.className = "status success";
        }
    } catch {
        if(status) {
            status.textContent = "No se pudieron cargar las playlists públicas";
            status.className = "status error";
        }
    } finally {
        hideLoadingPopup();
    }
}

// ==========================================
// 5. MANEJO DE UI (TÍTULOS, POPUPS, SCROLL)
// ==========================================

function showPlaylistTitle(name, count, unit = 'canciones') {
    const titleElem = document.getElementById("playlistTitle");
    if(titleElem) {
        titleElem.innerHTML = `
            <span class="playlist-main">${name}</span>
            <span class="playlist-count">(${count} ${unit})</span>
        `;
        titleElem.style.display = "block";
    }
}

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

function showCoffeeButton() {
    const bmc = document.querySelector('.bmc-container');
    if (bmc) {
        bmc.style.display = 'block';
    }
}

// ==========================================
// 6. EVENT LISTENERS
// ==========================================

// Botón "Generar cartas" (Unificado)
const loadUnifiedBtn = document.getElementById("loadUnifiedBtn");
if (loadUnifiedBtn) {
    loadUnifiedBtn.addEventListener("click", () => {
        const input = document.getElementById("spotifyInput");
        const url = input.value.trim();
        const status = document.getElementById("unifiedStatus");

        // Validación básica de URL de Spotify
        if (!url || !url.includes('spotify.com')) {
            status.textContent = "Introduce una URL válida de Spotify";
            status.className = "status error";
            return;
        }

        if (url.includes('/user/')) {
            loadFriendPlaylists(url);
            updateUrlWithInput(url, 'profile');
        } else if (url.includes('/playlist/') || url.includes('/track/') || url.includes('/album/')) {
            renderCards(url);
            updateUrlWithInput(url, 'spotify');
        } else {
            status.textContent = "URL no soportada (debe ser perfil, playlist o canción)";
            status.className = "status error";
        }
    });
}

// Paginación: Anterior
document.getElementById("prevPageBtn")?.addEventListener("click", () => {
    if (currentPage > 1) {
        showLoadingPopup();
        currentPage--;
        setTimeout(() => {
            renderPage(currentPage);
            hideLoadingPopup();
        }, 150);
    }
});

// Paginación: Siguiente
document.getElementById("nextPageBtn")?.addEventListener("click", () => {
    const totalPages = Math.ceil(allCards.length / PAGE_SIZE);
    if (currentPage < totalPages) {
        showLoadingPopup();
        currentPage++;
        setTimeout(() => {
            renderPage(currentPage);
            hideLoadingPopup();
        }, 150);
    }
});

// Init desde URL compartida al cargar la página
window.addEventListener("load", () => {
    const params = new URLSearchParams(window.location.search);
    const spotifyUrl = params.get("spotify");
    const profileUrl = params.get("profile");
    const input = document.getElementById("spotifyInput");

    if (spotifyUrl) {
        if(input) input.value = spotifyUrl;
        renderCards(spotifyUrl);
    } else if (profileUrl) {
        if(input) input.value = profileUrl;
        loadFriendPlaylists(profileUrl);
    }
});