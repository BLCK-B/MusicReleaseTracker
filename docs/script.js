let currentImage = 0;

const images = [
  'screenshot1.png',
  'screenshot2.png',
  'screenshot3.png'
];

const titles = [
  'One simple overview',
  'Choose your sources',
  'Customize to your needs'
];

function changeImage(direction) {
  currentImage = (currentImage + direction + images.length) % images.length;
  document.getElementById('gallery-image').src = images[currentImage];
  document.getElementById('gallery-title').textContent = titles[currentImage];
}
