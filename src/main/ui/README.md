# UI Package Structure

This directory contains all the UI-related files organized in a proper package structure following modern web development best practices.

## Directory Structure

```
src/main/resources/ui/
├── assets/                 # Static assets (CSS, JS, images, etc.)
│   ├── css/               # Stylesheets
│   │   └── styles.css     # Main application stylesheet
│   └── js/                # JavaScript files
│       ├── app.js         # Compiled JavaScript (production)
│       └── app.ts         # TypeScript source (development)
└── pages/                 # HTML pages and templates
    └── index.html         # Main application page
```

## Benefits of This Structure

1. **Separation of Concerns**: Assets are separated from pages
2. **Scalability**: Easy to add more CSS files, JS modules, or pages
3. **Maintainability**: Clear organization makes code easier to find and maintain
4. **Build Process**: Supports different build tools and processes
5. **Version Control**: Better for tracking changes to specific file types

## File Serving

The `UIWebConfig.java` configuration class handles serving these files:
- CSS files are served from `/css/` URL path
- JavaScript files are served from `/node/src/` URL path (with legacy `/js/` support)
- HTML pages are served from the root URL path

## Development Notes

- CSS and JS files have caching disabled in development mode
- The structure supports both development and production builds
- TypeScript files can be compiled to the js directory
- Additional assets (images, fonts) can be added under assets/

## Future Enhancements

This structure supports:
- Multiple CSS files for different themes or components
- Modular JavaScript/TypeScript architecture
- Component-based development
- Asset optimization and minification
- Progressive Web App features