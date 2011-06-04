class AddTaPoint < ActiveRecord::Migration
  def self.up
    add_column :tracks, :tag, :string
  end

  def self.down
    remove_column :tracks, :tag
  end
end
