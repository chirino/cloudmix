
namespace :website do

  desc 'Builds the website and copy the source to the output directory'
  task :build do
    begin
      Dir.glob('content/**/*.txt') do | file |
        outdir = 'output/' + File.split(file)[0]
        if not File.exists? outdir
          mkdir_p outdir
        end
        destfile = 'output/' + file
        if not File.exists? destfile
          cp file, destfile, :verbose => true
        end
      end
    end
  end

end  # namespace :website

#task :clobber => 'website:clobber'

# EOF
