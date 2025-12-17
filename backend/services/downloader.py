import yt_dlp
from pathlib import Path
import uuid
from services.video_processing import video_processor

class VideoDownloader:
    def __init__(self, download_dir: str = "uploads"):
        self.download_dir = Path(download_dir)
        self.download_dir.mkdir(exist_ok=True)

    def download_video(self, url: str) -> str:
        """
        Downloads a video from a URL (YouTube, generic) using yt-dlp.
        Returns the absolute path to the downloaded file.
        """
        file_id = str(uuid.uuid4())
        # Template: uploads/UUID.mp4
        output_template = str(self.download_dir / f"{file_id}.%(ext)s")

        ydl_opts = {
            'format': 'bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best',
            'outtmpl': output_template,
            'noplaylist': True,
        }

        if video_processor.ffmpeg_path and Path(video_processor.ffmpeg_path).is_absolute():
            ydl_opts['ffmpeg_location'] = video_processor.ffmpeg_path

        # Add robust options - REMOVED ignoreerrors to fail fast and see real error
        ydl_opts.update({
            'updatetime': False,
            'force_ipv4': True,
            'source_address': '0.0.0.0', # Bind to IPv4 interface
            'nocheckcertificate': True,
            'socket_timeout': 15,
            'retries': 10,
            # 'ignoreerrors': True, # Removed to debug
            # 'quiet': True,        # Removed to debug
            'no_warnings': True,
        })
        
        print(f"DEBUG: ydl_opts: {ydl_opts}")

        # DEBUG: Print version to ensure update worked
        import yt_dlp.version
        print(f"DEBUG: yt-dlp version: {yt_dlp.version.__version__}")

        # Attempt 1: Force IPv4 (Common Fix)
        # Attempt 2: Allow IPv6 (Fallback if IPv4 unavailable)
        attempts = [
            {"name": "Force IPv4", "opts": {**ydl_opts, 'force_ipv4': True, 'source_address': '0.0.0.0'}},
            {"name": "Standard/IPv6", "opts": {**ydl_opts, 'force_ipv4': False}} # Remove source_address
        ]
        
        # Clean up source_address from second attempt if it leaked from ydl_opts copy (it didn't, we used **ydl_opts)
        # But wait, ydl_opts has source_address from my previous edit? 
        # I should clean the base ydl_opts first.
        if 'source_address' in attempts[1]['opts']:
            del attempts[1]['opts']['source_address']

        last_error = None

        for attempt in attempts:
            print(f"DEBUG: Trying download strategy: {attempt['name']}")
            try:
                with yt_dlp.YoutubeDL(attempt['opts']) as ydl:
                    info = ydl.extract_info(url, download=True)
                    
                    if info is None:
                        raise Exception("yt-dlp extract_info returned None")
                    
                    # Success! Find file
                    file_id = str(uuid.uuid4()) # Only needed if we didn't define it outside loop?
                    # logic relies on the file_id defined at start of method.
                    # We generated file_id at line 16. It is constant for this call.
                    
                    # Check for file matching the UUID
                    # Since we set 'outtmpl' with the ID, it should be there.
                    for file in self.download_dir.glob(f"{file_id}.*"):
                        return str(file.absolute())
                    
                    # Fallback
                    filename = ydl.prepare_filename(info)
                    return str(Path(filename).absolute())
                    
            except Exception as e:
                print(f"Strategy {attempt['name']} failed: {e}")
                last_error = e
                # Continue to next attempt
        
        # If all failed
        print(f"All download strategies failed. Last error: {last_error}")
        raise last_error

downloader = VideoDownloader()
