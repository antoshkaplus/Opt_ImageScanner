
#include "image_scanner.hpp"


string ImageToScan::scan(int row)
{
    cout << "?" << endl;

    cout << row << endl;

    string str;
    cin >> str;
    return str;
}


int main() {
    int H, W, nb, nLetter;
    cin >> H >> W >> nb >> nLetter;
    ImageScanner scanner;
    auto res = scanner.restore(H, W, nb, nLetter);
    cout << "!" << endl;
    for (auto i = 0; i < H; ++i) {
        cout << res[i] << endl;
    }
    cout.flush();
}
