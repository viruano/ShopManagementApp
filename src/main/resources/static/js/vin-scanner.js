// =========================================================
// 📷 NATIVE BROWSER SHAPE DETECTION HARDWARE DRIVER CODE
// =========================================================
let activeLocalCameraMediaStream = null;
let hardwareScannerPollingLoopTimer = null;
let isCurrentlyProcessingFrame = false;
let nativeBarcodeDecoderInstance = null;

// Check on startup if the device motherboard supports hardware-accelerated scanning
if ('BarcodeDetector' in window) {
    nativeBarcodeDecoderInstance = new BarcodeDetector({
        formats: ['code_128', 'code_39', 'qr_code'] // Strictly focus on automotive VIN barcode parameters
    });
    console.log("Hardware-accelerated Barcode Detection engine successfully hooked into device motherboard.");
}

function activateLiveVinVideoScanner(event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }

    const wrapper = document.getElementById('vinVideoScannerHardwareContainer');
    if (wrapper) wrapper.classList.remove('hidden');

    if (activeLocalCameraMediaStream) return;

    // Wake up the exact, verified working native browser camera layout stream
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        navigator.mediaDevices.getUserMedia({
            video: {
                facingMode: "environment", // Enforces rear autofocus camera array
                width: { ideal: 1280 },
                height: { ideal: 720 }
            }
        })
            .then(function(stream) {
                activeLocalCameraMediaStream = stream;

                const videoElement = document.getElementById('nativeLiveVideoCameraShutterTrack');
                if (videoElement) {
                    videoElement.srcObject = stream;
                    videoElement.setAttribute("playsinline", true);
                    videoElement.play().then(function() {
                        // ⚡ FAST HARDWARE POLLING LAYER: Feeds frame snapshots directly to the phone's CPU chips every 300ms
                        hardwareScannerPollingLoopTimer = setInterval(executeNativeHardwareBarcodeExtractionPass, 300);
                    }).catch(function(e) { console.error(e); });
                }
            })
            .catch(function(err) {
                console.error("Native hardware stream rejected: ", err);
                alert("Camera blocked. Please verify your phone settings or ensure you are viewing this via an HTTPS secure link.");
                terminateLiveVinVideoScanner();
            });
    }
}

// 🧠 AUTOMATED DATA CAPTURE LAYER: Passes data straight to device processor chip matrices
function executeNativeHardwareBarcodeExtractionPass() {
    const videoElement = document.getElementById('nativeLiveVideoCameraShutterTrack');
    if (!videoElement || videoElement.paused || videoElement.ended || isCurrentlyProcessingFrame) return;

    // If the specific phone brand doesn't support hardware scanning, fall back gracefully to a prompt uploader message
    if (!nativeBarcodeDecoderInstance) {
        clearInterval(hardwareScannerPollingLoopTimer);
        const ticker = document.getElementById('liveOcrProcessingFeedbackTicker');
        if (ticker) {
            ticker.innerText = "❌ Device hardware scanning disabled. Please type or paste the 17-digit VIN code manually.";
            ticker.className = "text-[10px] font-black font-mono text-center text-rose-400 py-1 bg-slate-950 border border-slate-800 rounded";
        }
        return;
    }

    isCurrentlyProcessingFrame = true;

    // Hand the live running HTML5 video tag object directly to your phone's processor chips!
    nativeBarcodeDecoderInstance.detect(videoElement)
        .then(function(detectedBarcodesArray) {
            if (detectedBarcodesArray.length > 0) {
                // Grab the raw text string returned directly by the phone's operating system
                let rawExtractedTextToken = detectedBarcodesArray[0].rawValue;
                console.log("Motherboard barcode match returned: " + rawExtractedTextToken);

                // Clean manufacturer gaps, spaces, or specialized formatting dividers
                let parsedVinString = rawExtractedTextToken.replace(/[^a-zA-Z0-9]/g, '').trim().toUpperCase();

                // Trim prefix padding down to the standard 17 characters
                if (parsedVinString.length > 17) {
                    if (parsedVinString.startsWith("I") || parsedVinString.startsWith("Q") || parsedVinString.startsWith("9N")) {
                        parsedVinString = parsedVinString.substring(parsedVinString.length - 17);
                    } else {
                        parsedVinString = parsedVinString.substring(0, 17);
                    }
                }

                // 🎉 AUTO-FILL SUCCESS: Populates your template text input fields instantly!
                const targetInputField = document.getElementById('vehicleVinInputField');
                if (targetInputField) {
                    targetInputField.value = parsedVinString;
                    targetInputField.classList.add('ring-2', 'ring-emerald-500', 'bg-emerald-50/10');
                    targetInputField.dispatchEvent(new Event('change')); // Auto-fires your backend specs decoder
                }

                if (navigator.vibrate) navigator.vibrate(120); // Sharp haptic confirmation vibration click
                terminateLiveVinVideoScanner();
            } else {
                isCurrentlyProcessingFrame = false; // Open path to feed next video frame chunk straight to phone chips
            }
        })
        .catch(function(err) {
            console.error("Motherboard processing pass error: ", err);
            isCurrentlyProcessingFrame = false;
        });
}

function terminateLiveVinVideoScanner() {
    if (hardwareScannerPollingLoopTimer) {
        clearInterval(hardwareScannerPollingLoopTimer);
        hardwareScannerPollingLoopTimer = null;
    }

    if (activeLocalCameraMediaStream) {
        activeLocalCameraMediaStream.getTracks().forEach(track => track.stop());
        activeLocalCameraMediaStream = null;
    }

    isCurrentlyProcessingFrame = false;
    document.getElementById('vinVideoScannerHardwareContainer').classList.add('hidden');

    const videoElement = document.getElementById('nativeLiveVideoCameraShutterTrack');
    if (videoElement) {
        videoElement.srcObject = null;
    }
}