const path = require("path");

const cArrayPackagePath = path.join(
	__dirname,
	"..",
	"..",
	"..",
	"cdata",
	"cmetadata",
	"c3dclasses_js",
	"src",
	"ccore",
	"cdatastructures",
	"carray",
	"CArray.js"
);

require(cArrayPackagePath);

const values = [1, 2, 3, 4, 5];
values.shuffle();

console.log("Loaded package module:", cArrayPackagePath);
console.log("Array after CArray.js shuffle():", values);