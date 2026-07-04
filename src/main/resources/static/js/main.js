document.addEventListener('DOMContentLoaded', function () {

  /* Navbar shadow/shrink on scroll */
  var nav = document.querySelector('.site-navbar');
  if (nav) {
    var onScroll = function () {
      if (window.scrollY > 12) nav.classList.add('is-scrolled');
      else nav.classList.remove('is-scrolled');
    };
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
  }

  /* Scroll-reveal animations */
  var revealEls = document.querySelectorAll('.reveal');
  if (revealEls.length) {
    if ('IntersectionObserver' in window) {
      var io = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            io.unobserve(entry.target);
          }
        });
      }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });
      revealEls.forEach(function (el) { io.observe(el); });
    } else {
      revealEls.forEach(function (el) { el.classList.add('is-visible'); });
    }
  }

  /* Admin sidebar: highlight the link matching the current section */
  var sidebar = document.querySelector('.admin-sidebar');
  if (sidebar) {
    var path = window.location.pathname;
    var links = sidebar.querySelectorAll('a.nav-link[href^="/admin/"]');
    var bestMatch = null;
    links.forEach(function (link) {
      var href = link.getAttribute('href');
      if (path === href || path.startsWith(href + '/')) {
        if (!bestMatch || href.length > bestMatch.getAttribute('href').length) {
          bestMatch = link;
        }
      }
    });
    if (bestMatch) bestMatch.classList.add('active');
  }

  /* Admin sidebar: mobile toggle (shared fragment layout) */
  var sidebarToggle = document.getElementById('adminSidebarToggle');
  if (sidebarToggle && sidebar) {
    sidebarToggle.addEventListener('click', function (e) {
      e.stopPropagation();
      sidebar.classList.toggle('show');
    });
    document.addEventListener('click', function (e) {
      if (sidebar.classList.contains('show') &&
          !sidebar.contains(e.target) &&
          e.target !== sidebarToggle && !sidebarToggle.contains(e.target)) {
        sidebar.classList.remove('show');
      }
    });
  }

  /* Back to top button */
  var backToTop = document.querySelector('.back-to-top');
  if (backToTop) {
    var toggleBtn = function () {
      if (window.scrollY > 400) backToTop.classList.add('is-visible');
      else backToTop.classList.remove('is-visible');
    };
    toggleBtn();
    window.addEventListener('scroll', toggleBtn, { passive: true });
    backToTop.addEventListener('click', function () {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }
});
