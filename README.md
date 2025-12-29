# 🌟 Noxiapp - Habit Tracking & Fitness App

A comprehensive Android application for tracking habits, managing workouts, and achieving personal goals.

## 📱 Features

### 🎯 Habit Tracking
- **Predefined Habits**: Quick setup with pre-configured habits
  - 💧 Water intake tracking
  - 📚 Reading (pages or minutes)
  - 💊 Vitamin tracking
  - 🧘 Meditation sessions
  - 🏃 Exercise completion
- **Custom Habits**: Create personalized habits with flexible tracking options
- **Progress Visualization**: Track your daily, weekly, and monthly progress
- **Streak Tracking**: Maintain and visualize your habit streaks

### 💪 Workout Management
- **Exercise Library**: Extensive collection of predefined exercises
- **Custom Workouts**: Create and save personalized workout programs
- **Progress Tracking**: Log weights, reps, and notes for each exercise
- **Calendar Integration**: View workout history by date
- **Program Management**: Save and load different workout routines

### 🏆 Achievements System
- Unlock achievements as you progress
- Track your accomplishments
- Stay motivated with milestone rewards

### 📊 Analytics & Insights
- **Calendar View**: Visual representation of your activity
- **Statistics**: Detailed insights into your habits and workouts
- **Profile Management**: Track personal information and goals

### 🔐 User Authentication
- Secure Firebase Authentication
- Email/Password login
- User profile management

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: 
  - Room (Local storage)
  - Firebase Firestore (Cloud sync)
- **Authentication**: Firebase Auth
- **Dependency Injection**: Hilt/Dagger (if applicable)
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)

## 📦 Project Structure

```
com.noxi.noxiapp/
├── data/
│   ├── local/          # Room database DAOs
│   ├── repository/     # Data repositories
│   └── models/         # Data classes
├── ui/
│   ├── screens/        # Compose screens
│   ├── components/     # Reusable UI components
│   └── theme/          # App theming & styling
└── MainActivity.kt     # Main entry point
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or higher
- Android SDK 34
- Firebase account

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Noxiuur/Noxiapp.git
   cd Noxiapp
   ```

2. **Firebase Setup**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create a new project or use existing one
   - Download `google-services.json`
   - Place it in the `app/` directory

3. **Configure Firebase Security Rules**
   
   **Firestore Rules:**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId}/{document=**} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

4. **Build and Run**
   ```bash
   ./gradlew build
   ```
   Or open the project in Android Studio and click Run ▶️

## 📸 Screenshots

> Add screenshots of your app here

## 🔒 Security

- Firebase API keys are excluded from version control
- User data is protected with Firebase Security Rules
- Authentication required for all data operations
- Local data encrypted with Room

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is private and proprietary.

## 👤 Author

**Noxiuur**
- GitHub: [@Noxiuur](https://github.com/Noxiuur)

## 📞 Support

For support, please open an issue in the GitHub repository.

---

**Made with ❤️ using Kotlin & Jetpack Compose**
