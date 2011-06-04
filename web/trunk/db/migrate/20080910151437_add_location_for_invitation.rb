class AddLocationForInvitation < ActiveRecord::Migration
  def self.up
    add_column :invitations, :location, :string
  end

  def self.down
    remove_column :invitations,:location
  end
end
