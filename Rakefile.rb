WE_VERSION = '0.85'

require 'rake/rdoctask'
require 'rake/testtask'
require 'rake/packagetask'
require 'rake/clean'
require 'rdoc/rdoc'
require 'find'

task :default => [:test, :doc]

# Testing
desc 'Run the test suite'
Rake::TestTask.new do |t|
  t.libs << "test"
  t.test_files = FileList["test/tc_mediawiki.rb",
                          "test/tc_dotgraph.rb",
                          "test/tc_pixvis.rb"]
  t.verbose = true
  t.warning = true
end

# Documentation
rd_main = "README"
rd_include = ["mediawiki.rb", "mywikis.rb", "mediawiki/", "util/", 
              "README", "INSTALL", "TODO"]

desc 'Generate the documentation in html'
Rake::RDocTask.new do |rd|
  rd.main = rd_main
  rd.rdoc_files.include(*rd_include)
  rd.options << "-S"
  rd.template = 'mwrdoc'
end

# the following is a hack to circumvent rdoc limitations
# (we want external links to open outside the frameset).
# Perhaps we should really switch to yard.
task :doc => :rdoc do
  warn 'changing links'
  Find.find('./html') do |path|
    if FileTest.file?(path) && (path =~ /.html$/)
      File.open(path, 'r+') do |file|
        content = file.read
        file.rewind
        file << content.gsub(/<a href="http:\/\//, 
                             '<a target="_blank" href="http://')
      end
    end
  end
end

# We test YARD as alternative to RDoc
#require 'yard'
#YARD::Rake::YardocTask.new do |t|
#  t.files   = rd_include.collect { |f| f.gsub(/\/$/) { '/**/*.rb' }}
#  t.options = ['-o', 'yardoc',
#               '--title', 'WikiExplorator Documentation',
#               '--files', 'INSTALL',]
#end

task :package => :doc

desc 'Package files for release'
Rake::PackageTask.new('mwparser', WE_VERSION) do |p|
  globs = %w(mediawiki util test html).collect { |w| w + '/**/*' } +
    %w(Rakefile.rb mediawiki.rb mywikis.rb) + ['Documentation/tutorial.pdf']
  p.package_files.include(*globs)
  p.need_tar_bz2 = true
end
