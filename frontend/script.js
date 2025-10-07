let accessToken = null;
let refreshToken = null;

const output = document.getElementById('output');

document.getElementById('signInBtn').addEventListener('click', async () => {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    console.log(username);
    console.log(password);

    const res = await fetch('http://localhost:8080/auth/sign-in', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
    });

    console.log(res);

    const data = await res.json();
    if (res.ok) {
        accessToken = data.access_token;
        refreshToken = data.refresh_token;
        output.textContent = `Signed in!\nAccess Token: ${accessToken}\nRefresh Token: ${refreshToken}`;
    } else {
        output.textContent = `Sign in error: ${JSON.stringify(data)}`;
    }
});

document.getElementById('refreshBtn').addEventListener('click', async () => {
    if (!refreshToken) {
        output.textContent = "No refresh token available. Sign in first.";
        return;
    }

    const res = await fetch('http://localhost:8080/auth/refresh', {
        method: 'POST',
        headers: {
            'X-Refresh-Token': refreshToken
        }
    });

    const data = await res.json();
    if (res.ok) {
        accessToken = data.access_token;
        refreshToken = data.refresh_token;
        output.textContent = `Tokens refreshed!\nAccess Token: ${accessToken}\nRefresh Token: ${refreshToken}`;
    } else {
        output.textContent = `Refresh error: ${JSON.stringify(data)}`;
    }
});

document.getElementById('callHomeBtn').addEventListener('click', async () => {
    if (!accessToken) {
        output.textContent = "No access token available. Sign in first.";
        return;
    }

    const res = await fetch('http://localhost:8080/home', {
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    const data = await res.json();
    if (res.ok) {
        output.textContent = `Response from /home:\n${JSON.stringify(data, null, 2)}`;
    } else {
        output.textContent = `Call error: ${JSON.stringify(data)}`;
    }
});