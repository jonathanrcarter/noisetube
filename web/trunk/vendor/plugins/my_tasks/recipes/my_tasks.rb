

namespace :passenger do

  desc "Restarts Passenger"
  task :restart do
    puts "\n\n=== Restarting Passenger ===\n\n"
    run "sudo touch #{current_path}/tmp/restart.txt"
  end

  desc "Creates the production log if it does not exist"
  task :create_production_log do
    unless File.exist?(File.join(current_path, 'log', 'production.log'))
      puts "\n\n=== Creating Production Log! ===\n\n"
      run "touch #{File.join(current_path, 'log', 'production.log')}"
    end
  end
  
end

namespace :noisetube do

  desc "restart server"
  task :update do
    puts "\n\n=== Updating code  ===\n\n"
    run "cd /var/noisetube/current;sudo svn update"
    passenger::restart
    puts "\n\n=== Restarting Background Job ===\n\n"
    delayed_job::restart
  end

  desc "migrations with export"
  task :migrations do
    deploy::update_code
    deploy::migrate
    deploy::symlink
    noisetube::restart
  end

end
