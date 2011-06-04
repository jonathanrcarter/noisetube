class Addpatterntotrack < ActiveRecord::Migration
  def self.up
    add_column :tracks, :highvariation, :integer, :default=>0
    add_column :tracks, :highexposure, :integer, :default=>0
    add_column :users, :twitter_acoount, :string
    add_column :users, :twitter_password, :string
  end

  def self.down
  end
end
