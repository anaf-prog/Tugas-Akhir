/**
 * Scroll langsung ke elemen (tanpa animasi)
 * @param {string} elementId - ID dari elemen tujuan
 */

document.addEventListener("DOMContentLoaded", function () {
    if ('scrollRestoration' in history) {
        history.scrollRestoration = 'manual';
    }

    if (window.location.hash) {
        const targetId = window.location.hash.substring(1);
        setTimeout(() => scrollToElement(targetId), 100);
    }

    document.addEventListener('click', function (e) {
        const link = e.target.closest('.pagination a');
        if (link) {
            e.preventDefault();
            const href = link.getAttribute('href');

            if (href.includes('#device-list')) {
                loadPaginationContent(href.split('#')[0], 'device-list');
            } else if (href.includes('#log-ping')) {
                loadPaginationContent(href.split('#')[0], 'log-ping');
            } else if (href.includes('#failover-list')) {
                loadPaginationContent(href.split('#')[0], 'failover-list');
            } else {
                loadPaginationContent(href.split('#')[0], null);
            }
        }
    });

    const scrollLinks = document.querySelectorAll("[data-scroll]");
    scrollLinks.forEach(link => {
        link.addEventListener("click", function (e) {
            e.preventDefault();
            scrollToElement(this.getAttribute("data-scroll"));
        });
    });
});






// function scrollToElement(elementId) {
//     const target = document.getElementById(elementId);
//     if (!target) return;

//     const navbar = document.querySelector('.navbar');
//     const navbarHeight = navbar ? navbar.offsetHeight : 0;
//     const yOffset = -navbarHeight - 20; // offset untuk navbar dan sedikit spacing
//     const targetPosition = target.getBoundingClientRect().top + window.pageYOffset + yOffset;

//     // Scroll langsung tanpa animasi
//     window.scrollTo(0, targetPosition);
// }

// /**
//  * Load konten menggunakan AJAX (Fetch API)
//  * @param {string} url - URL untuk pagination
//  * @param {string} targetId - ID elemen yang menjadi fokus scroll
//  */
// function loadPaginationContent(url, targetId) {
//     fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
//         .then(response => {
//             if (!response.ok) throw new Error('Gagal memuat data');
//             return response.text();
//         })
//         .then(html => {
//             // Update konten utama (ganti dengan ID container kamu)
//             const contentContainer = document.getElementById('content-container');
//             if (contentContainer) {
//                 contentContainer.innerHTML = html;
//             }

//             // Setelah konten dimuat, scroll ke target
//             setTimeout(() => {
//                 scrollToElement(targetId);
//             }, 100);

//             // Update URL di address bar tanpa reload
//             history.pushState({}, '', url);
//         })
//         .catch(error => console.error('Error:', error));
// }

// document.addEventListener("DOMContentLoaded", function () {
//     // Handle initial page load dengan hash
//     if (window.location.hash) {
//         const targetId = window.location.hash.substring(1); // Remove the '#' character
//         setTimeout(() => {
//             scrollToElement(targetId);
//         }, 100);
//     }

//     // Event listener untuk link pagination
//     document.addEventListener('click', function (e) {
//         const link = e.target.closest('.pagination a');
//         if (link) {
//             e.preventDefault();
//             const href = link.getAttribute('href');

//             if (href.includes('#device-list')) {
//                 loadPaginationContent(href.split('#')[0], 'device-list');
//             } else if (href.includes('#log-ping')) {
//                 loadPaginationContent(href.split('#')[0], 'log-ping');
//             } else if (href.includes('#failover-list')) {
//                 loadPaginationContent(href.split('#')[0], 'failover-list');
//             } else {
//                 // Default: load tanpa scroll ke elemen tertentu
//                 loadPaginationContent(href.split('#')[0], null);
//             }
//         }
//     });

//     // Event listener untuk semua link dengan atribut data-scroll
//     const scrollLinks = document.querySelectorAll("[data-scroll]");
//     scrollLinks.forEach(link => {
//         link.addEventListener("click", function (e) {
//             e.preventDefault();
//             const targetId = this.getAttribute("data-scroll");
//             scrollToElement(targetId);
//         });
//     });
// });
























// function smoothScrollTo(elementId) {
//     const target = document.getElementById(elementId);
//     if (!target) return;

//     const navbar = document.querySelector('.navbar');
//     const navbarHeight = navbar ? navbar.offsetHeight : 0;
//     const yOffset = -navbarHeight - 20; // offset untuk navbar dan sedikit spacing
//     const targetPosition = target.getBoundingClientRect().top + window.pageYOffset + yOffset;

//     // Scroll langsung tanpa animasi
//     window.scrollTo(0, targetPosition);
// }

// // Event listener untuk semua link dengan hash dalam URL
// document.addEventListener("DOMContentLoaded", function () {
//     // Handle initial page load with hash
//     if (window.location.hash) {
//         const targetId = window.location.hash.substring(1); // Remove the '#' character
//         setTimeout(() => {
//             smoothScrollTo(targetId);
//         }, 100);
//     }

//     // Event listener untuk semua link pagination
//     const paginationLinks = document.querySelectorAll(".pagination a");
//     paginationLinks.forEach(link => {
//         link.addEventListener("click", function () {
//             // Cek jika link ini adalah link pagination untuk device list
//             if (this.getAttribute('href') && this.getAttribute('href').includes('#device-list')) {
//                 sessionStorage.setItem('scrollToDeviceList', 'true');
//             }
//             // Cek jika link ini adalah link pagination untuk log ping
//             else if (this.getAttribute('href') && this.getAttribute('href').includes('#log-ping')) {
//                 sessionStorage.setItem('scrollToLogPing', 'true');
//             }
//             // Cek jika link ini adalah link pagination untuk failover list
//             else if (this.getAttribute('href') && this.getAttribute('href').includes('#failover-list')) {
//                 sessionStorage.setItem('scrollToFailoverList', 'true');
//             }
//         });
//     });

//     // Handle scroll setelah halaman dimuat (setelah navigasi)
//     setTimeout(() => {
//         if (sessionStorage.getItem('scrollToDeviceList') === 'true') {
//             smoothScrollTo('device-list');
//             sessionStorage.removeItem('scrollToDeviceList');
//         }
//         if (sessionStorage.getItem('scrollToLogPing') === 'true') {
//             smoothScrollTo('log-ping');
//             sessionStorage.removeItem('scrollToLogPing');
//         }
//         if (sessionStorage.getItem('scrollToFailoverList') === 'true') {
//             smoothScrollTo('failover-list');
//             sessionStorage.removeItem('scrollToFailoverList');
//         }
//     }, 100);
// });

// // Event listener untuk semua link dengan atribut data-scroll (jika ada)
// document.addEventListener("DOMContentLoaded", function () {
//     const scrollLinks = document.querySelectorAll("[data-scroll]");
//     scrollLinks.forEach(link => {
//         link.addEventListener("click", function (e) {
//             e.preventDefault();
//             const targetId = this.getAttribute("data-scroll");
//             smoothScrollTo(targetId);
//         });
//     });
// });
