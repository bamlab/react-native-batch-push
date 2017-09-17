
Pod::Spec.new do |s|
  s.name         = "RNBatchPush"
  s.version      = "1.0.0"
  s.summary      = "RNBatchPush"
  s.description  = <<-DESC
                  RNBatchPush
                   DESC
  s.homepage     = "https://github.com/bamlab/react-native-batch-push"
  s.license      = { :type => "MIT", :file => "LICENSE" }
  s.author       = { "author" => "lagrange.louis@gmail.com" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/bamlab/react-native-batch-push.git", :tag => "master" }
  s.source_files  = "RNBatchPush/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  