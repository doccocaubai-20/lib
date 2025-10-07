// Xử lý menu người dùng dropdown
document.addEventListener("DOMContentLoaded", () => {
  const userMenu = document.querySelector(".user-menu");
  if (userMenu) {
    userMenu.addEventListener("click", () => {
      alert("Mở trang hồ sơ người dùng!");
    });
  }

  // Giả lập tìm kiếm
  const searchBtn = document.querySelector(".search-bar button");
  if (searchBtn) {
    searchBtn.addEventListener("click", () => {
      const query = document.querySelector(".search-bar input").value;
      alert(`Tìm kiếm: ${query}`);
    });
  }
});
