// HD Video Player Controls

document.addEventListener('DOMContentLoaded', () => {
    const video = document.getElementById('videoPlayer');
    const playPauseBtn = document.getElementById('playPauseBtn');
    const volumeSlider = document.getElementById('volumeSlider');
    const fullScreenBtn = document.getElementById('fullScreenBtn');

    // Play/Pause functionality
    playPauseBtn.addEventListener('click', () => {
        if (video.paused) {
            video.play();
            playPauseBtn.textContent = 'Pause';
        } else {
            video.pause();
            playPauseBtn.textContent = 'Play';
        }
    });

    // Volume control
    volumeSlider.addEventListener('input', () => {
        video.volume = volumeSlider.value;
    });

    // Fullscreen mode
    fullScreenBtn.addEventListener('click', () => {
        if (!document.fullscreenElement) {
            video.requestFullscreen().catch(err => {
                console.error(`Error attempting to enable fullscreen: ${err.message}`);
            });
        } else {
            if (document.exitFullscreen) {
                document.exitFullscreen();
            }
        }
    });

    // Update button text when video is played or paused
    video.addEventListener('play', () => {
        playPauseBtn.textContent = 'Pause';
    });

    video.addEventListener('pause', () => {
        playPauseBtn.textContent = 'Play';
    });

    // Update volume slider when video volume changes
    video.addEventListener('volumechange', () => {
        volumeSlider.value = video.volume;
    });

    // Ensure video quality settings (if source changes dynamically, this can be adjusted)
    video.setAttribute('playsinline', ''); // For mobile compatibility
});
