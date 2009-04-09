require 'rake/rdoctask'
require 'rake/testtask'
require 'rake/packagetask'
require 'rake/clean'
require 'rdoc/rdoc'

task :default => [:test, :html]

# Testing
desc 'Run the test suite'
Rake::TestTask.new do |t|
  t.libs << "test"
  t.test_files = FileList["test/tc_mediawiki.rb","test/tc_dotgraph.rb"]
  t.verbose = true
  t.warning = true
end

# Documentation
rd_main = "mediawiki.rb"
rd_include = [rd_main, "mywikis.rb", "mediawiki/", "util/"]

desc 'Generate the documentation in html'
Rake::RDocTask.new(:html) do |rd|
  rd.main = rd_main
  rd.rdoc_files.include(*rd_include)
  rd.options << "-S"
end

desc 'Package files for release'
Rake::PackageTask.new('mwparser','0.8') do |p|
  globs = %w(mediawiki util test html).collect { |w| w + '/**/*' } +
    %w(Rakefile.rb mediawiki.rb mywikis.rb)
  p.package_files.include(*globs)
  p.need_tar_bz2 = true
end
