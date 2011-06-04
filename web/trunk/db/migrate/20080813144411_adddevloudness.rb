class Adddevloudness < ActiveRecord::Migration
  def self.up
     add_column :sounds,"devloudness",:float
  end

  def self.down
    remove_column :sounds,"devloudness"
  end
end
