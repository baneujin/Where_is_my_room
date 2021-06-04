var dropdownMenu = document.getElementsByClassName('dropdown-menu');
var dropdownButton = document.getElementsByClassName('dropdown-button');

dropdownButton[0].addEventListener('click', function(event) {

  if (this.active) {
    dropdownMenu[0].classList.remove("active");
  } else {
    dropdownMenu[0].classList.add("active");
  }
    
  this.active = !this.active;
})

dropdownButton[0].active = false;