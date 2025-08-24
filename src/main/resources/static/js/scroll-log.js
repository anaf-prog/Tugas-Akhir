/**
 * Smooth Scroll to an element by ID
 * @param {string} elementId - ID dari elemen tujuan
 * @param {number} duration - Durasi animasi dalam milidetik (default: 1500)
 */
function smoothScrollTo(elementId, duration = 1500) {
    const target = document.getElementById(elementId);
    if (!target) return;

    const yOffset = -10; // offset (misal untuk navbar tetap)
    const targetPosition = target.getBoundingClientRect().top + window.pageYOffset + yOffset;
    const startPosition = window.pageYOffset;
    const distance = targetPosition - startPosition;
    let startTime = null;

    function animationScroll(currentTime) {
        if (startTime === null) startTime = currentTime;
        const timeElapsed = currentTime - startTime;

        // Fungsi easing (easeInOutCubic)
        const ease = (t) => t < 0.5
            ? 4 * t * t * t
            : 1 - Math.pow(-2 * t + 2, 3) / 2;

        const progress = Math.min(timeElapsed / duration, 1);
        const easedProgress = ease(progress);

        window.scrollTo(0, startPosition + distance * easedProgress);

        if (timeElapsed < duration) {
            requestAnimationFrame(animationScroll);
        }
    }

    requestAnimationFrame(animationScroll);
}

// Event listener untuk semua link dengan atribut data-scroll
document.addEventListener("DOMContentLoaded", function () {
    const scrollLinks = document.querySelectorAll("[data-scroll]");
    scrollLinks.forEach(link => {
        link.addEventListener("click", function (e) {
            e.preventDefault();
            const targetId = this.getAttribute("data-scroll");
            smoothScrollTo(targetId);
        });
    });
});