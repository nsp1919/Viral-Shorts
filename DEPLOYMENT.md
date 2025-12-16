# AutoShorts Deployment Guide

Complete guide for deploying the AutoShorts application.

## 1. Backend Deployment (Render)

### Step 1: Push to GitHub
Ensure your code is pushed to GitHub (you already have https://github.com/nsp1919/Auto-Shorts).

### Step 2: Deploy on Render

1. Go to [render.com](https://render.com) and sign up/login
2. Click "New +" → "Web Service"
3. Connect your GitHub repository
4. Configure:
   - **Name**: `autoshorts-api`
   - **Root Directory**: `backend`
   - **Environment**: `Python 3`
   - **Build Command**: `pip install -r requirements.txt`
   - **Start Command**: `uvicorn main:app --host 0.0.0.0 --port $PORT`

5. Add Environment Variables:
   - `OPENAI_API_KEY`: Your OpenAI API key
   - `RENDER`: `true`

6. Click "Create Web Service"

> ⚠️ **FFmpeg Note**: Render's default Python environment doesn't include FFmpeg. 
> You may need to use a Docker deployment or switch to a plan that supports `apt-get install ffmpeg`.

### Step 3: Note Your Backend URL
After deployment, your backend URL will be: `https://autoshorts-api.onrender.com`

---

## 2. Frontend Deployment (Vercel)

### Step 1: Deploy to Vercel

1. Go to [vercel.com](https://vercel.com) and sign up/login
2. Click "Add New..." → "Project"
3. Import your GitHub repository
4. Configure:
   - **Framework Preset**: Next.js
   - **Root Directory**: `frontend`

5. Add Environment Variable:
   - `NEXT_PUBLIC_API_URL`: `https://autoshorts-api.onrender.com` (your Render backend URL)

6. Click "Deploy"

### Step 2: Note Your Frontend URL
After deployment, your frontend URL will be something like: `https://autoshorts.vercel.app`

---

## 3. Update CORS (if needed)

After deployment, update `backend/main.py` with your actual Vercel URL:

```python
origins = [
    # ... existing origins ...
    "https://your-actual-project.vercel.app",
]
```

---

## 4. Android App Configuration

### Step 1: Update Web App URL

Edit `android/app/src/main/java/com/nspcreativehub/autoshorts/MainActivity.kt`:

```kotlin
companion object {
    private const val WEB_APP_URL = "https://your-actual-project.vercel.app"
}
```

### Step 2: Build APK

1. Open `android/` folder in Android Studio
2. Wait for Gradle sync
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. Find APK at `android/app/build/outputs/apk/debug/app-debug.apk`

---

## Quick Checklist

- [ ] Backend deployed to Render
- [ ] `OPENAI_API_KEY` environment variable set on Render
- [ ] Frontend deployed to Vercel  
- [ ] `NEXT_PUBLIC_API_URL` environment variable set on Vercel
- [ ] Android app URL updated in `MainActivity.kt`
- [ ] APK built successfully
- [ ] Test end-to-end on Android device
