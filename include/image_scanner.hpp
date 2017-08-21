#pragma once


#include "util.hpp"
#include "image_scanner.hpp"


class ImageToScan {
public:

    static string scan(int row);
};

class ImageScanner {
public:

    vector<string> restore(int H, int W, int nb, int nLetter);

};