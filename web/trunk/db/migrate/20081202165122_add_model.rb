class AddModel < ActiveRecord::Migration
  def self.up
    add_column :invitations,:phone,:string
  end

  def self.down
    remove_column :invitations,:phone
  end
end
