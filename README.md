# Mini Search Engine

[![Live Demo](https://img.shields.io/badge/Live-Demo-brightgreen)](https://mini-search-engine.vercel.app)

Miniature Search Engine demonstrates a fully functional search engine complete with web crawling, autocomplete, and advanced ranking algorithms. Whether you're a developer looking to learn or just curious about how search engines work, this project offers a hands-on experience with modern web technologies.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Usage](#usage)
- [Running Tests](#running-tests)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Features

- **Web Crawling**: Automatically indexes web pages starting from a seed URL.
- **Autocomplete**: Provides real-time suggestions as users type their search queries.
- **Search Functionality**: Uses TF-IDF and PageRank to deliver relevant search results.
[to be implemented]: <> (- **Analytics**: Tracks and displays popular search queries.)

## Technologies Used

- **Backend**: Spring Boot, Gradle, Neon PostgreSQL, Redis
- **Frontend**: React, Vercel
- **Other**: Docker for containerization

## Installation

### Prerequisites

- Java 17 or higher
- Gradle
- Node.js and npm
- Access to PostgreSQL(Serverless) and Redis instances

### Backend Setup

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/mini-search-engine.git
   ```
2. Navigate to the backend directory:
   ```
   cd mini-search-engine/backend
   ```
3. Set up environment variables for database and Redis connections. Refer to `.env.example` for required variables.
4. Build the project:
   ```
   gradle build
   ```
5. Run the application:
   ```
   gradle bootRun
   ```

### Frontend Setup

1. Navigate to the frontend directory:
   ```
   cd ../frontend
   ```
2. Install dependencies:
   ```
   npm install
   ```
3. Start the development server:
   ```
   npm run dev
   ```

## Usage

- **Web Crawling**: Trigger the crawler by sending a GET request to `/api/crawl?seedUrl=https://example.com`. This can be setup as a separate cron job.
- **Search**: Use the search bar on the homepage to query indexed pages.
- **Autocomplete**: Start typing in the search bar to see suggestions.
[to be implemented]: <> (- **Analytics**: View top search queries by sending a GET request to `/api/analytics/top-queries`.
)
## Running Tests

### Backend Tests

Navigate to the backend directory and run:
```
gradle test
```

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes. Ensure your code follows the project's coding standards and includes tests where applicable.

## License

This project is licensed under the **MIT License** - see the LICENSE file for details.

## Contact

For questions or issues, please contact at https://omaryan.vercel.app or open an issue on GitHub.