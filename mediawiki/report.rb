#!/usr/bin/ruby -w
# :title: Mediawiki-Reports - Ruby Lib
# = Mediawiki Reports
# This file extends the Mediawiki module with a set of reports.

require 'tmpdir'
require 'erb'

module Mediawiki
  # Report module. Container for all Report classes.
  module Report
    # The directory the templates are found.
    TemplateDir = File.join(File.dirname(__FILE__),'reports')

    # creates a Report of type _type_ for the _wiki_. _type_ may be
    # one of <tt>:txt</tt>, <tt>:html</tt>, <tt>tex</tt> or <tt>pdf</tt>.
    #
    # For parameter description see the new method of the corresponding
    # class in Report.
    def Report.new(wiki, type=:txt, params={})
      {
        :txt    => PlainText,
        :html   => HTML,
        :tex    => LaTeX,
        :latex  => LaTeX,
        :pdf    => PDF
      }[type].new(wiki, params)
    end

    # Base class for all reports. Not to be used directly.
    class Base
      # creates a Report
      #
      # Dont use this class directly but one of the subclasses shaped for
      # your report type.
      #
      # A report is generated by running erb on a template file.
      #
      # _wiki_ is the wiki to be reported.
      #
      # _params_:
      # <tt>:template</tt> => "default":: the base name of the template
      # <tt>:templatedir</tt> => TemplateDir:: where templates are to be found
      # <tt>:language</tt> => wiki.language:: language of the template file
      # <tt>:templatefile</tt>=><i>templatedir/template.language.ttype</i>::
      #   the full filename of the template. 
      # <tt>:type</tt>::
      #   type of the report. Used to find the template (if :ttype is not set).
      # <tt>:ttype</tt>:: 
      #   file extension for the template file. Used if this differs from
      #   the report type.
      # If the template file for the selected language does not exist,
      # <i>templatedir/template.ttype</i> is tried.
      def initialize(wiki, params={})
        @wiki = wiki
        params = {
          :templatedir => TemplateDir,
          :template => 'default',
          :language => @wiki.language,
        }.merge(params)

        @type = params[:type]
        ttype = params[:ttype] || @type

        # searching for the template ...
        tmpl = "#{params[:template]}.#{params[:language]}.#{ttype}"
        tmpl_dir = params[:templatedir]
        lang = true
        tmpl_full_l = File.join(tmpl_dir, tmpl)
        tmpl_full = tmpl_full_l
        begin
          @erb = ERB.new(File.read(tmpl_full))
        rescue Errno::ENOENT
          if lang
            tmpl = "#{params[:template]}.#{ttype}"
            tmpl_full = File.join(tmpl_dir, tmpl)
            warn "Reading '#{tmpl_full_l}' failed. Trying '#{tmpl_full}'."
            lang = false
            retry
          else
            @erb = nil
            raise Errno::ENOENT.new("#{tmpl_full_l} - #{tmpl_full}")
          end
        end
      end

      # Creates the report and returns it (is overwritten in subclasses
      # and subclasses may return a filename instead of the full report, 
      # see there)
      def generate
        @erb.result(binding) 
      end

      def inspect
        "#<#{self.class}: #{@wiki.to_s}>"
      end
    end

    # Plain Text reports which do not need temporary files
    class PlainText < Base
      # see Base#new for parameters. :ttype is set to "txt".
      def initialize(wiki, params={})
        super(wiki, { :ttype => 'txt' }.merge(params))
      end
      # pretty prints this report on the screen.
      def pp
        @generated ||= generate
        puts @generated
      end
    end

    # Base class for all reports who need auxiliary files. 
    # Not to be used directly.
    class DirBase < Base
      # creates a Report
      #
      # Dont use this class directly but one of the subclasses shaped for
      # your report type.
      #
      # _wiki_ is the wiki to be reported.
      #
      # _params_:
      # <tt>:basedir</tt>:: 
      #   the base directory wherein the output directory will be created.
      #   Defaults to the systems TEMP directory. Must exist.
      # <tt>:outputdir</tt>:: 
      #   Directory where the report is generated (i.e. the working
      #   directory where all auxiliary file as well as the resulting report 
      #   file are generated). If not given an uniq directory name will be
      #   generated. If the outputdir does not exist it will be created.
      #   The parent directory of the output dir must exist.
      # <tt>:mode</tt>::
      #   The file access permissions the outputdir will be set to. Defaults 
      #   to 0700.
      # <tt>:filename</tt>:: 
      #   the name of the report file. To be given without path but with
      #   extension. Defaults to <tt>report</tt>.<i>type</i>.
      # <tt>:type</tt>::
      #   type of the report. Used to find the template (if :ttype is not set)
      #   and to generate the default report filename.
      # <tt>:ttype</tt>:: 
      #   file extension for the template file. Used if this differs from
      #   the report type.
      # For all further parameters see Base#new
      def initialize(wiki, params={})
        super(wiki, params)
        @basedir = params[:basedir] || Dir.tmpdir
        @filename = params[:filename] || ('report.' + @type)
        mode = params[:mode] || 0700
        mode = mode.oct if mode.kind_of?(String)
        if @outputdir = params[:outputdir]
          @outputdir = File.join(@basedir, @outputdir) unless @outputdir[0]==?/
        else
          # create an uniq directory 
          dirname = "MW-#{'%08x' % $$}-#{"%012x" % (Time.now.to_f*100000)}"
          i = 0
          @outputdir = File.join(@basedir, dirname)
          begin
            Dir.mkdir(@outputdir)
          rescue Errno::EEXIST
            i+=1
            if i<100
              @outputdir = File.join(@basedir, dirname + "-#{i}")
              retry
            else # should never happen!
              raise Errno::EEXIST.new(File.join(@basedir, dirname) + " .. " +
                                      @outputdir) 
            end
          end
        end
        Dir.mkdir(@outputdir) unless File.directory?(@outputdir)
        File.chmod(mode, @outputdir)
      end

      # generates the report and returns its full filename.
      def generate
        Dir.chdir(@outputdir) do
          File.open(@filename, 'w') { |f| f << @erb.result(binding) }
        end
        File.join(@outputdir, @filename)
      end
    end
    
    # HTML Reports
    class HTML < DirBase
      # see DirBase#new for parameters. :ttype is set to "html".
      def initialize(wiki, params={})
        super(wiki, {:type => 'html'}.merge(params))
      end
    end

    # LaTeX Reports. You may prefer PDF.
    class LaTeX < DirBase
      # see DirBase#new for parameters. :ttype is set to "tex".
      def initialize(wiki, params={})
        super(wiki, {:type => 'tex'}.merge(params))
      end
    end

    # PDF reports. uses LaTeX for PDF generation.
    class PDF < DirBase
      TeXname = 'report.tex'

      # see DirBase#new for parameters. 
      # :ttype is set to "tex", :type is "pdf".
      def initialize(wiki, params={})
        super(wiki, {:type => 'pdf', :ttype => 'tex'}.merge(params))
        @texname = TeXname
      end

      # generates a pdf report and returns its full filename.
      #
      # _loops_ gives the number of times pdflatex is run on the LaTeXfile
      # (you may need this if you want to generate a table of contents).
      # You can alternatively set the number of loops to some value _x_ by 
      # setting <tt>@loops=</tt>_x_ in the report template file.
      def generate(loops=nil)
        Dir.chdir(@outputdir) do
          File.open(@texname, 'w') { |f| f << @erb.result(binding) }
          pdfname = @filename.sub(/\.pdf$/,'') # as pdflatex wants no extension
          loops ||= @loops || 1
          loops.times do
            system('pdflatex', '-jobname', pdfname, 
                   '-interaction', 'batchmode', @texname)
          end
        end
        File.join(@outputdir, @filename)
      end
    end
  end
  class Wiki
    # Generates a report for the wiki. Returns a String with the report
    # for plain report formats and a String with a filename for complex
    # report formats. See Report.new for a description of parameters.
    def report(type=:txt, params={})
      Report.new(self, type, params).generate
    end
  end
end
