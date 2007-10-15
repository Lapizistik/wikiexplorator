require 'rake/rdoctask'
require 'rake/testtask'
require 'rdoc/rdoc'

puts $@

# Testing
desc 'Run the test suite'
Rake::TestTask.new do |t|
  t.libs << "test"
  t.test_files = FileList["test/tc_mediawiki.rb"]
  t.verbose = true
  t.warning = true
end

# Documentation
rd_main = "mediawiki.rb"
rd_include = [rd_main, "dotgraph.rb", "wio.rb", "mediawiki/"]

desc 'Generate the documentation in html'
Rake::RDocTask.new(:html) do |rd|
  rd.main = rd_main
  rd.rdoc_files.include(*rd_include)
  rd.options << "-S"
end
