require 'tempfile'

# This modul is necessary because the windows implementation of
# <tt>epstopdf</tt> (at least the one provided by miktex) is not
# able to read from standard in and write to file.
# Simply redirecting standard out to a file with ">filename" did not
# work with gnuplot.
module Epstopdf
  # the ps2pdf command. "%i" is the placeholder for the input,
  # "%o" for the output filename.
  PS2PDF = ENV['RB_PS2PDF'] || 'epstopdf --outfile %o %i'

  def epstopdf(outfile) # :yields: filename
    tf = Tempfile.new(['pspdf','.eps']) # create a tempfile
    tf.close # close it as we only need it as placeholder for the binary
    tmpname = tf.path # the filename
    yield(tmpname) # and call the block where the eps is created
    ps2pdf = PS2PDF.gsub('%i', tmpname).gsub('%o', outfile)
    begin
      unless system(ps2pdf)
        es = $?.exitstatus
        if es==127 # hopefully this holds true on several OSes
          raise "Executable not found when trying `#{ps2pdf}'."
        elsif es>128 # hopefully this holds true on several OSes
          raise "Executable aborted while running `#{ps2pdf}'."
        else
          raise "Error running `#{ps2pdf}'."
        end
      end
    ensure
      tf.unlink # and remove it
    end
  end
end
