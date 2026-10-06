import SwiftUI

struct ProductImageView: View {
    let url: String?
    var body: some View {
        ZStack {
            Color(hex: 0xF6F6F7)
            if let url, let parsed = URL(string: url) {
                AsyncImage(url: parsed) { phase in
                    if let image = phase.image { image.resizable().scaledToFill() }
                    else { Image(systemName: "frying.pan").font(.largeTitle).foregroundStyle(PMColor.primary) }
                }
            } else {
                Image(systemName: "frying.pan").font(.largeTitle).foregroundStyle(PMColor.primary)
            }
        }
        .clipped()
    }
}
