/**
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 100 }, // Ramp up to 100 users over 1 minute
        { duration: '3m', target: 500 }, // Ramp up to 500 users and hold for 3 minutes
        { duration: '1m', target: 0 },   // Ramp down to 0 users over 1 minute
    ],
};

export default function () {

    const res =  http.get('http://9.223.64.113:9000/catalog');

    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    // Sleep for 1 second between requests so we don't accidentally DDoS your laptop
    sleep(1);
}
 **/


/**
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 100 }, // Ramp up to 100 users over 1 minute
        { duration: '3m', target: 500 }, // Hold 500 users for 3 minutes
        { duration: '1m', target: 0 },   // Ramp down
    ],
};

export default function () {
    // Random page forces a DB hit every time — cache can't help here
    const page = Math.floor(Math.random() * 10) + 1;
    const res = http.get(`http://9.223.64.113:9000/catalog/search?query=the&page=${page}`);

    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    sleep(1);
}

 **/

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 100 }, // Ramp up to 100 users
        { duration: '3m', target: 500 }, // Hold at 500 users
        { duration: '1m', target: 0 },   // Ramp down
    ],
};

const BASE_URL = 'http://9.223.64.113:9000/catalog';

const GENRES = ['Fantasy', 'Dystopian', 'Thriller', 'Mystery', 'Science Fiction', 'Horror', 'Classic', 'Young Adult'];
const KEYWORDS = ['the', 'art', 'war', 'game', 'dark', 'great'];

function browseBooks() {
    const page = Math.floor(Math.random() * 5) + 1;
    return http.get(`${BASE_URL}?page=${page}`);
}

function searchByKeyword() {
    const word = KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
    const page = Math.floor(Math.random() * 5) + 1;
    return http.get(`${BASE_URL}/search?query=${word}&page=${page}`);
}

function searchByGenre() {
    const genre = GENRES[Math.floor(Math.random() * GENRES.length)];
    return http.get(`${BASE_URL}/search?genre=${encodeURIComponent(genre)}&page=1`);
}

function viewBook() {
    const bookNumber = String(Math.floor(Math.random() * 50) + 1).padStart(3, '0');
    return http.get(`${BASE_URL}/B${bookNumber}`); // ← fixed: /catalog/B001 not /catalog/products/B001
}

export default function () {
    const roll = Math.random();

    if (roll < 0.50) {
        const res = browseBooks();
        check(res, { 'listing 200': (r) => r.status === 200 });

    } else if (roll < 0.75) {
        const res = searchByKeyword();
        check(res, { 'search 200': (r) => r.status === 200 });

    } else if (roll < 0.90) {
        const res = searchByGenre();
        check(res, { 'genre 200': (r) => r.status === 200 });

    } else {
        const res = viewBook();
        check(res, { 'product 200': (r) => r.status === 200 });
    }

    sleep(1);
}


