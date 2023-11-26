export const renderElement = (el: any): any => {
    try {
        if (el[0] == 0) {
            return 'null';
        }
        if (el[0] == 1) {
            return el[1];
        }
        if (el[0] == 2) {
            return el[1];
        }
        if (el[0] == 3) {
            return `"` + el[1] + `"`;
        }
        if (el[0] == 4) {
            return btoa(el[1]);
        }
    } catch { }

    return el;
};

export const renderValue = (el: any): any => {
    try {
        if (el[0] == 0) {
            return 'null';
        }
        if (el[0] == 1) {
            return el[1];
        }
        if (el[0] == 2) {
            return el[1];
        }
        if (el[0] == 3) {
            return el[1];
        }
        if (el[0] == 4) {
            return btoa(el[1]);
        }
    } catch { }

    return el;
};