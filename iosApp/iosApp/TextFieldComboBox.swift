//
//  TextFieldComboBox.swift
//  iosApp
//
//  Created by Jesus Daniel Medina Cruz on 17/01/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

struct TextFieldComboBox: View{
    @State var value = ""
    var dropDownItem: [String]  = ["item 1", "item 2", "item 3"]
    var body: some View {
    
        HStack{
            TextField("Select an item", text: $value)
                .textFieldStyle(.roundedBorder)
 
            Menu {
                ForEach(dropDownItem, id: \.self){ item in
                    Button(item) {
                        self.value = item
                    }
                }
            } label: {
                VStack(spacing: 5){
                    Image(systemName: "chevron.down")
                        .font(.title3)
                }
            }
 
        } .padding(.horizontal)
    }
}
