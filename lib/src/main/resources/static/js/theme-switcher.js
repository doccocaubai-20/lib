// src/main/resources/static/js/theme-switcher.js
document.addEventListener('DOMContentLoaded', () => {
    const themeToggleBtn = document.getElementById('theme-toggle');
    const sunIcon = document.querySelector('.theme-icon-sun');
    const moonIcon = document.querySelector('.theme-icon-moon');

    // Hàm để áp dụng theme và cập nhật icon
    const applyTheme = (theme) => {
        if (theme === 'dark') {
            document.body.classList.add('dark-mode');
            if(sunIcon) sunIcon.style.display = 'inline-block';
            if(moonIcon) moonIcon.style.display = 'none';
        } else {
            document.body.classList.remove('dark-mode');
            if(sunIcon) sunIcon.style.display = 'none';
            if(moonIcon) moonIcon.style.display = 'inline-block';
        }
    };

    // Lấy theme đã lưu hoặc theme mặc định của hệ thống
    const preferredTheme = localStorage.getItem('theme') || 
                           (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');

    // Áp dụng theme khi tải trang
    applyTheme(preferredTheme);

    // Bắt sự kiện click vào nút
    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', () => {
            const currentTheme = document.body.classList.contains('dark-mode') ? 'dark' : 'light';
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

            applyTheme(newTheme);
            localStorage.setItem('theme', newTheme);
        });
    }
});