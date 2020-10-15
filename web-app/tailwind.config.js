module.exports = {
    purge: [
        "./**/*.html",
        "./**/*.tsx",
    ],
    theme: {
        extend: {},
    },
    variants: {},
    plugins: [],
    future: {
        removeDeprecatedGapUtilities: true,
        purgeLayersByDefault: true,
    },
}
