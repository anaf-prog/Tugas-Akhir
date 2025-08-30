// document.addEventListener('click', function (e) {
//     const link = e.target.closest('.ajax-page-link'); // pakai class khusus
//     if (link) {
//         e.preventDefault(); // Cegah reload halaman
//         const url = link.getAttribute('href');

//         fetch(url, {
//             method: 'GET',
//             headers: {
//                 'X-Requested-With': 'XMLHttpRequest' // Supaya controller tahu ini AJAX
//             }
//         })
//         .then(response => response.text())
//         .then(html => {
//             // Parse HTML hasil response
//             const parser = new DOMParser();
//             const doc = parser.parseFromString(html, 'text/html');

//             // Ambil ulang isi #device-container
//             const newContent = doc.querySelector('#device-container');
//             if (newContent) {
//                 document.querySelector('#device-container').innerHTML = newContent.innerHTML;
//                 window.history.pushState({}, '', url); // Update URL tanpa reload
//             }
//         })
//         .catch(error => {
//             console.error('Error:', error);
//             alert('Gagal memuat data.');
//         });
//     }
// });
