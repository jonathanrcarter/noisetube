class AddCommenToUser < ActiveRecord::Migration
  def self.up
     add_column :users,:comment,:text
  end

  def self.down
    remove_column :users,:comment
  end
end
