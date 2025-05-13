# Note Taking Mobile App

A comprehensive, feature-rich note taking application for Android that allows users to create, edit, and organize their thoughts with rich media attachments and flexible organization options.

## Features

### Core Functionality

-   **Create and Edit Notes**:
    -   Add images, audio recordings, and drawings
    -   Interactive checklists for task management
    -   Auto-save functionality to prevent data loss
-   **Note Organization**:
    -   Pin important notes to the top of the list
    -   Sort by title, creation date, or last modification date
    -   Filter and search through notes
-   **Flexible Layout Options**:
    -   Toggle between grid and list view layouts
    -   Adjustable card sizes and densities
-   **Visual Customization**:
    -   Light and dark theme support with seamless transitions
    -   Adjustable text size (Small, Medium, Large)
-   **Persistent Storage**: All notes automatically saved to local database

### Media Attachments

-   **Images**:
    -   Capture photos directly from camera
    -   Import multiple images from gallery
    -   Automatic image resizing and optimization
    -   Thumbnail generation for note previews
-   **Audio Notes**:
    -   Record voice memos with integrated recorder
    -   Playback controls for audio notes
    -   Auto-naming of audio files with timestamps
-   **Drawing Tools**:
    -   Integrated sketch pad for drawings and diagrams
    -   Multiple brush colors with color picker
    -   Undo functionality for drawing mistakes
    -   Save drawings as part of notes

### Task Management

-   **Checklist Support**:
    -   Convert notes to interactive checklists
    -   Check/uncheck items to track progress
    -   Reorder checklist items
    -   Mixed content notes with text and checklists

### Data Management

-   **Local Storage**:
    -   Room database for efficient data persistence
    -   Automatic saving of all changes
-   **Trash System**:
    -   Soft delete with move to trash functionality
    -   Restore notes from trash
    -   Permanently delete single notes or empty trash
-   **User Accounts**:
    -   Firebase Authentication integration
    -   Email and password authentication
    -   Profile management
-   **Cloud Integration**:
    -   Firebase Firestore for note synchronization
    -   Firebase Storage for media files
    -   Multi-device synchronization

## Technical Overview

### Architecture

The application follows MVVM (Model-View-ViewModel) architecture with clean separation of concerns:

-   **Presentation Layer**: Activities, Fragments, and custom views that handle UI rendering and user interactions
-   **ViewModel Layer**: ViewModels that manage UI state, handle business logic, and coordinate data operations
-   **Data Layer**: Repositories that abstract data sources and provide a clean API for the ViewModels
-   **Domain Layer**: Use cases and business logic (implemented within repositories and ViewModels)

Key architectural components:

-   Repository pattern for data operations
-   LiveData for reactive UI updates and lifecycle awareness
-   ViewModels for preserving UI state during configuration changes
-   Base classes for code reuse and consistent behavior

### Key Components

#### UI Layer

-   **Activity Structure**:

    -   `BaseActivity`: Foundation class with theme handling and common functionality
    -   `HomeActivity`: Main entry point displaying the note list
    -   `NewNoteActivity`: Creation of new notes
    -   `EditNoteActivity`: Modification of existing notes
    -   `DeletedNoteActivity`: Management of deleted notes
    -   `SettingActivity`: User preferences and app configuration
    -   `DrawActivity`: Dedicated drawing interface

-   **Custom UI Components**:
    -   `NoteAdapter`: RecyclerView adapter for displaying note items
    -   Custom views for specialized media interactions
    -   Material Design components for consistent UX

#### ViewModel Layer

-   `BaseNoteViewModel`: Abstract class providing shared note functionality
-   `HomeViewModel`: Manages the list of notes with sorting and filtering
-   `NewNoteViewModel`: Handles creation of new notes
-   `EditNoteViewModel`: Manages updates to existing notes
-   `DeletedNoteViewModel`: Manages trash operations
-   `SettingViewModel`: Handles user preferences and settings

#### Data Layer

-   **Local Storage**:

    -   Room Database (v2.7.1) with DAOs for structured data
    -   `NoteDao`: Interface for note CRUD operations
    -   `Note` entity: Main data model with rich content support
    -   `CheckListItem` entity: For task management
    -   `FileManager`: Utility for managing media files on device storage
    -   Custom type converters for complex data types

-   **Remote Storage** (partially implemented):

    -   Firebase Firestore for structured data
    -   Firebase Storage for media files
    -   `FirestoreRepository`: Interface for cloud operations
    -   `FirestoreNote`: Data model for Firestore integration

-   **Settings Management**:
    -   `SettingRepository`: Manages app preferences via SharedPreferences
    -   Reactive updates to configuration changes

### Libraries & Dependencies

-   **AndroidX and Core Components**:

    -   AndroidX AppCompat and Core: `androidx.appcompat:appcompat`, `androidx.core:core`
    -   Material Components: `com.google.android.material:material:1.1.0`
    -   ConstraintLayout: `androidx.constraintlayout:constraintlayout`
    -   Activity: `androidx.activity:activity`

-   **Architecture Components**:

    -   Room: `androidx.room:room-runtime:2.7.1`, `androidx.room:room-compiler:2.7.1`
    -   Lifecycle Components: `androidx.lifecycle:lifecycle-viewmodel:2.8.0`, `androidx.lifecycle:lifecycle-livedata:2.8.0`

-   **Firebase Integration**:

    -   Firebase BOM: `com.google.firebase:firebase-bom:32.8.0`
    -   Authentication: `com.google.firebase:firebase-auth`
    -   Firestore: `com.google.firebase:firebase-firestore`
    -   Storage: `com.google.firebase:firebase-storage`

-   **Image Processing**:

    -   Glide: `com.github.bumptech.glide:glide:4.14.2`
    -   CircleImageView: `de.hdodenhof:circleimageview:3.1.0`

-   **Drawing and UI Components**:

    -   SignaturePad: `com.github.gcacace:signature-pad:1.2.0`
    -   ColorPicker: `com.github.kristiyanP:colorpicker:v1.1.10`

-   **Utilities**:
    -   Gson: `com.google.code.gson:gson:2.10.1`

## Usage Guide

### Creating a Note

1. Open the app and tap the floating "+" button in the bottom right corner
2. Enter a title and content for your note
3. To add media attachments:
    - Tap the image icon to add photos from camera or gallery
    - Tap the microphone icon to record audio
    - Tap the pencil icon to create drawings
    - Tap the checklist icon to add interactive checklist items
4. Tap the save button when finished

### Managing Notes

-   **Pin/Unpin**: Long press on a note and select "Pin" from the menu
-   **Edit**: Tap on any note to open it for editing
-   **Delete**: Long press on a note and select "Delete" to move it to trash
-   **Change View**: Access settings to switch between list and grid views
-   **Sort Notes**: Access settings to change the sort order (by date, title, etc.)

### Trash Management

1. Tap the trash icon in the top navigation bar to access deleted notes
2. Recover notes by selecting "Restore" option
3. Permanently delete by selecting "Delete forever"
4. Use "Empty trash" to delete all notes in trash

### Settings and Customization

Access the settings screen by tapping the gear icon in the top navigation:

-   **Text Size**: Adjust the text size across the app
-   **Theme**: Toggle between light and dark themes
-   **Layout**: Choose between list or grid layouts for notes
-   **Sort By**: Select note sorting preference

## Setup and Installation

### Prerequisites

-   Android Studio Arctic Fox or newer
-   SDK version 25+ (Android 7.1 Nougat)
-   JDK 11
-   Google Firebase account for Authentication and Firestore

### Build and Run

1. Clone the repository
    ```
    git clone https://github.com/ductung3008/note-taking-mobile-app.git
    ```
2. Open the project in Android Studio
3. Connect Firebase by adding your `google-services.json` to the app directory:
    - Create a Firebase project at [firebase.google.com](https://firebase.google.com)
    - Register your app with package name `com.haui.notetakingapp`
    - Download the `google-services.json` file and place it in the `app/` directory
    - Enable Authentication and Firestore in your Firebase console
4. Sync Gradle and build the project
5. Run on a device or emulator (minimum API level 25)

### Testing

The app includes both unit tests and instrumented tests:

-   Run unit tests: `./gradlew test`
-   Run instrumented tests: `./gradlew connectedAndroidTest`

## Project Structure

```
app/src/main/
  ├── java/com/haui/notetakingapp/
  │   ├── data/                # Data layer
  │   │   ├── local/           # Local database (Room)
  │   │   │   ├── dao/         # Data Access Objects
  │   │   │   │   └── NoteDao.java
  │   │   │   ├── entity/      # Database entities
  │   │   │   │   ├── Note.java
  │   │   │   │   └── CheckListItem.java
  │   │   │   ├── Converters.java
  │   │   │   ├── FileManager.java
  │   │   │   ├── NoteAppGlideModule.java
  │   │   │   └── NoteDatabase.java
  │   │   ├── remote/          # Remote data sources
  │   │       ├── firebase/    # Firebase integration
  │   │       │   └── FirestoreRepository.java
  │   │       ├── model/       # Remote data models
  │   │           └── FirestoreNote.java
  │   ├── repository/          # Repositories
  │   │   ├── AuthRepository.java
  │   │   ├── NoteRepository.java
  │   │   └── SettingRepository.java
  │   ├── ui/                  # UI layer
  │   │   ├── base/            # Base classes
  │   │   │   └── BaseActivity.java
  │   │   ├── home/            # Home screen
  │   │   │   ├── HomeActivity.java
  │   │   │   └── NoteAdapter.java
  │   │   ├── note/            # Note screens (new, edit)
  │   │   │   ├── base/
  │   │   │   │   └── BaseNoteActivity.java
  │   │   │   ├── DeletedNoteActivity.java
  │   │   │   ├── DrawActivity.java
  │   │   │   ├── EditNoteActivity.java
  │   │   │   └── NewNoteActivity.java
  │   │   ├── setting/         # Settings screen
  │   │       └── SettingActivity.java
  │   ├── utils/               # Utilities
  │   │   └── DateTimeUtils.java
  │   ├── viewmodel/           # ViewModels
  │       ├── BaseNoteViewModel.java
  │       ├── DeletedNoteViewModel.java
  │       ├── EditNoteViewModel.java
  │       ├── HomeViewModel.java
  │       ├── NewNoteViewModel.java
  │       └── SettingViewModel.java
  └── res/                     # Resources
      ├── drawable/            # Icons and images
      ├── layout/              # UI layouts
      ├── menu/                # Menu resources
      ├── navigation/          # Navigation graphs
      ├── values/              # App resources (strings, styles, etc.)
      └── xml/                 # XML configuration files
```

## Implementation Details

### Note Entity Structure

The core `Note` entity contains:

```java
@Entity(tableName = "notes")
public class Note implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;              // UUID for unique identification
    private String title;           // Note title
    private String content;         // Main text content
    private long createdAt;         // Creation timestamp
    private long updatedAt;         // Last modification timestamp
    private List<String> imagePaths;    // Paths to saved images
    private List<String> audioPaths;    // Paths to recorded audio
    private List<String> drawingPaths;  // Paths to saved drawings
    private List<CheckListItem> checklistItems;  // Checklist items
    private boolean isPinned;       // Whether note is pinned
    private boolean isDeleted;      // Soft delete indicator

    // Constructors, getters, and setters
}
```

### Room Database Integration

Room is used for local data persistence with custom type converters for complex data types:

```java
@Database(entities = {Note.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class NoteDatabase extends RoomDatabase {
    // Implementation details
}

public class Converters {
    // Type converters for complex data types like Lists and custom objects
}
```

### Media File Management

Media files (images, audio, drawings) are stored in app-specific directories and referenced in the Note entity:

```java
public class FileManager {
    // Methods for saving, loading, and managing media files
}
```

## Future Enhancements

-   **Note Sharing**: Share notes via various channels and collaborative editing
-   **Rich Text Formatting**: Advanced formatting options (bold, italic, headings, etc.)
-   **Categories and Tags**: Improved organization with custom labels
-   **End-to-End Encryption**: Enhanced privacy for sensitive notes
-   **Import/Export**: Support for importing and exporting notes in various formats
-   **Widgets**: Home screen widgets for quick access to notes
-   **Biometric Authentication**: Fingerprint/face unlock for sensitive notes

## Performance Considerations

-   Efficient image loading and caching with Glide
-   Pagination for large note collections
-   Background processing for media operations
-   Optimized database queries with Room

## Acknowledgments

-   Thanks to the open-source libraries used in this project
-   Material Design guidelines
-   Firebase platform for authentication and cloud storage
