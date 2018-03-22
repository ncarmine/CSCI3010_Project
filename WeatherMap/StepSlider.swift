//
//  StepSlider.swift
//  WeatherMap
//
//  Copyright © 2018 ncarmine. All rights reserved.
//

import Foundation
import UIKit

class StepSlider: UISlider {
    let values: [Float]
    var lastIndex: Float?
    let callback: (Float) -> Void
    
    init(frame: CGRect, values: [Float], callback: @escaping (_ newValue: Float) -> Void) {
        self.values = values
        self.callback = callback
        super.init(frame: frame)
        self.addTarget(self, action: #selector(handleValueChange), for: .valueChanged)
        
        self.minimumValue = 0
        self.maximumValue = Float(values.count-1)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func handleValueChange(sender: UISlider) {
        let newIndex = ceil(sender.value)
        self.setValue(newIndex, animated: true)
        if lastIndex == nil || newIndex != lastIndex! {
            self.callback(self.values[Int(newIndex)])
        }
    }
}
