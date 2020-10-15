module.exports = {
    "roots": [
        "<rootDir>/src",
    ],
    "moduleNameMapper": {
        "^/@/(.*)$": "<rootDir>/src/$1",
    },
    "testMatch": [
        "**/?(*.)+(spec|test).+(ts|tsx|js)",
    ],
    "transform": {
        "^.+\\.(ts|tsx)$": "ts-jest",
    },
}
