function renderFooter() {
    const footer = document.getElementById("footer");
    if (!footer) return;

    const currentYear = new Date().getFullYear();

    footer.innerHTML = `
        <footer class="footer">
            <div class="footer-brand">
                <h3>Clinic Management System</h3>
                <p>&copy; ${currentYear} Clinic Management System. All rights reserved.</p>
            </div>
            <div class="footer-links">
                <div class="footer-column">
                    <h4>Company</h4>
                    <a href="/about">About</a>
                    <a href="/careers">Careers</a>
                    <a href="/press">Press</a>
                </div>
                <div class="footer-column">
                    <h4>Support</h4>
                    <a href="/account">Account</a>
                    <a href="/help">Help Center</a>
                    <a href="/contact">Contact</a>
                </div>
                <div class="footer-column">
                    <h4>Legals</h4>
                    <a href="/terms">Terms</a>
                    <a href="/privacy">Privacy Policy</a>
                    <a href="/licensing">Licensing</a>
                </div>
            </div>
        </footer>`;
}

window.renderFooter = renderFooter;

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", renderFooter);
} else {
    renderFooter();
}
