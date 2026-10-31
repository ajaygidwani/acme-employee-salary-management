/**
 * ACME Employee & Salary Management - Frontend Dev & Proxy Server
 * Pure Node.js (Zero external dependencies)
 *
 * Serves the AngularJS frontend on port 3000 and proxies /api requests
 * to the Spring Boot backend on port 8080 to prevent CORS issues.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = parseInt(process.env.PORT, 10) || 3000;
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';
const parsedBackend = new URL(BACKEND_URL);

const STATIC_DIR = __dirname;

const MIME_TYPES = {
  '.html': 'text/html; charset=UTF-8',
  '.js': 'application/javascript; charset=UTF-8',
  '.css': 'text/css; charset=UTF-8',
  '.json': 'application/json; charset=UTF-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon'
};

function addCorsHeaders(res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With');
}

function handleProxy(req, res) {
  addCorsHeaders(res);

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const options = {
    hostname: parsedBackend.hostname,
    port: parsedBackend.port || (parsedBackend.protocol === 'https:' ? 443 : 80),
    path: req.url,
    method: req.method,
    headers: {
      ...req.headers,
      host: `${parsedBackend.hostname}:${parsedBackend.port || 80}`
    }
  };

  const proxyReq = http.request(options, (backendRes) => {
    // Add CORS headers to backend response
    const headers = { ...backendRes.headers };
    headers['access-control-allow-origin'] = '*';
    headers['access-control-allow-methods'] = 'GET, POST, PUT, DELETE, OPTIONS';
    headers['access-control-allow-headers'] = 'Content-Type, Authorization, X-Requested-With';

    res.writeHead(backendRes.statusCode, headers);
    backendRes.pipe(res);
  });

  proxyReq.on('error', (err) => {
    console.error(`[Proxy Error] ${req.method} ${req.url} -> ${err.message}`);
    addCorsHeaders(res);
    res.writeHead(502, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      error: 'Bad Gateway',
      message: `Could not connect to backend at ${BACKEND_URL}. Ensure Spring Boot is running on port ${parsedBackend.port || 8080}.`,
      details: err.message
    }));
  });

  req.pipe(proxyReq);
}

function handleStatic(req, res) {
  const parsedUrl = url.parse(req.url);
  let pathname = parsedUrl.pathname;

  if (pathname === '/') {
    pathname = '/index.html';
  }

  // Security: prevent path traversal
  const safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');
  const filePath = path.join(STATIC_DIR, safePath);

  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      // Fallback to index.html for SPA routing
      const indexPath = path.join(STATIC_DIR, 'index.html');
      fs.readFile(indexPath, (readErr, content) => {
        if (readErr) {
          res.writeHead(404, { 'Content-Type': 'text/plain' });
          res.end('404 Not Found');
          return;
        }
        res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
        res.end(content);
      });
      return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    fs.readFile(filePath, (readErr, content) => {
      if (readErr) {
        res.writeHead(500, { 'Content-Type': 'text/plain' });
        res.end('500 Internal Server Error');
        return;
      }
      res.writeHead(200, { 'Content-Type': contentType });
      res.end(content);
    });
  });
}

const server = http.createServer((req, res) => {
  // If API request, forward to backend
  if (req.url.startsWith('/api')) {
    handleProxy(req, res);
  } else {
    handleStatic(req, res);
  }
});

server.listen(PORT, () => {
  console.log('==========================================================');
  console.log(' ACME Employee & Salary Management - Frontend Server');
  console.log('==========================================================');
  console.log(` Frontend running at : http://localhost:${PORT}`);
  console.log(` Backend proxy target : ${BACKEND_URL}`);
  console.log(` API requests mapped  : http://localhost:${PORT}/api/* -> ${BACKEND_URL}/api/*`);
  console.log('==========================================================');
});
